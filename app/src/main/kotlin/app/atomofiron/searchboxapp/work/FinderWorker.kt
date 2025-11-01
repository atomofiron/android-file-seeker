package app.atomofiron.searchboxapp.work

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import app.atomofiron.common.util.extension.debugDelay
import app.atomofiron.common.util.extension.get
import app.atomofiron.common.util.extension.invoke
import app.atomofiron.common.util.extension.logE
import app.atomofiron.common.util.extension.replace
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.android.NativeBridge
import app.atomofiron.searchboxapp.android.Notifications
import app.atomofiron.searchboxapp.android.tryShow
import app.atomofiron.searchboxapp.android.updateChannel
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.CacheConfig
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.FilesSearchTask
import app.atomofiron.searchboxapp.model.finder.ItemMatch
import app.atomofiron.searchboxapp.model.finder.QueryParams
import app.atomofiron.searchboxapp.model.finder.SearchResult.Files
import app.atomofiron.searchboxapp.model.finder.SearchState
import app.atomofiron.searchboxapp.model.finder.SearchTask
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch
import app.atomofiron.searchboxapp.screens.main.MainActivity
import app.atomofiron.searchboxapp.utils.Codes
import app.atomofiron.searchboxapp.utils.ExplorerUtils.resolveType
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toProperties
import app.atomofiron.searchboxapp.utils.canForegroundService
import app.atomofiron.searchboxapp.utils.ifCanNotice
import app.atomofiron.searchboxapp.utils.mutate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import uniffi.native_lib.Meta
import uniffi.native_lib.NameSearchProgress
import uniffi.native_lib.SimpleResult
import uniffi.native_lib.TextSearchProgress
import uniffi.native_lib.TypedMeta
import javax.inject.Inject

@SuppressLint("InlinedApi")
private const val UPDATING_FLAG = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

private const val KEY_EXCEPTION = "KEY_EXCEPTION"
private const val KEY_CANCELLED = "KEY_CANCELLED"

class FinderWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    @Serializable
    data class Params(
        val query: QueryParams,
        val type: Type,
        val maxDepth: Int,
        val targets: List<ByteArray>,
        val asSu: Boolean,
    ) {
        @Serializable
        sealed interface Type
        @Serializable
        data class Names(val excludeDirs: Boolean) : Type
        @Serializable
        data class Text(val maxSize: Long) : Type
    }

    private val taskMutex = Mutex()
    // todo remove deleting files from results
    private lateinit var task: FilesSearchTask
    private lateinit var cacheConfig: CacheConfig

    @Inject
    lateinit var finderStore: FinderStore
    @Inject
    lateinit var notifications: NotificationManagerCompat
    @Inject
    lateinit var appScope: AppScope
    @Inject
    lateinit var preferenceStore: PreferenceStore
    @Inject
    lateinit var workManager: WorkManager

    init {
        DaggerInjector.appComponent.inject(this)
    }

    private fun Params.searchText(type: Params.Text) = appScope.launch {
        val result = NativeBridge.findText(query, refs(), maxDepth = maxDepth, maxSize = type.maxSize, asSu) { match ->
            appScope {
                updateTask {
                    val new = when (match) {
                        is TextSearchProgress.Ok -> {
                            val lineIndex = match.line?.toInt() ?: return@appScope
                            val lineMatch = TextLineMatch(match.offset.toLong(), match.length.toInt())
                            var itemMatch = result.matches
                                .find { it.item.ref.theSame(match.path) }
                                ?.let { it as ItemMatch.Multiply }
                            val countTotal = when (itemMatch) {
                                null -> result.countTotal.inc()
                                else -> result.countTotal
                            }
                            itemMatch = itemMatch ?: NodeRef(match.path).let {
                                val node = NativeBridge.type(it, asSu).value?.toNode() ?: it.toNode()
                                ItemMatch.Multiply(node)
                            }
                            val matches = itemMatch.matchesMap.getOrPut(lineIndex) { mutableListOf() }
                            matches.add(lineMatch)
                            val new = result.matches.mutate {
                                replace(itemMatch.copy(count = itemMatch.count.inc())) {
                                    it.path == itemMatch.path
                                }
                            }
                            result.copy(count = result.count.inc(), matches = new, countTotal = countTotal)
                        }
                        is TextSearchProgress.End -> {
                            val itemMatch = result.matches.find { it.item.ref.theSame(match.v1) }
                            if (itemMatch != null) {
                                return@appScope
                            }
                            result.copy(countTotal = result.countTotal.inc())
                        }
                        is TextSearchProgress.Err -> return@appScope // todo
                    }
                    copyWith(result = new)
                }
            }
        }
        updateTask {
            val error = (result as? SimpleResult.Err)?.v1
            toEnded(error = error)
        }
    }

    private suspend inline fun updateTask(transform: FilesSearchTask.() -> FilesSearchTask) {
        taskMutex.withLock {
            task = task.transform()
        }
        finderStore.addOrUpdate(task.cast())
    }

    private fun Params.searchNames(type: Params.Names) = appScope {
        NativeBridge.findNames(query, refs(), maxDepth, type.excludeDirs, asSu) { match ->
            appScope {
                when (match) {
                    is NameSearchProgress.Ok -> updateTask {
                        val itemMatch = ItemMatch.Single(match.v1.toNode())
                        val matches = result.matches.toMutableList()
                        matches.replace(itemMatch) { it.path == itemMatch.path }
                        copyWith(result.copy(count = count.inc(), matches = matches, countTotal = result.countTotal.inc()))
                    }
                    is NameSearchProgress.Err -> Unit // todo
                }
            }
        }
    }

    override suspend fun doWork(): Result {
        val params = inputData.get<Params>()
        if (params == null) {
            logE("Query is null")
            return Result.success()
        }
        if (params.query.query.isEmpty()) {
            logE("Query is empty")
            return Result.success()
        }
        if (context.canForegroundService()) {
            notifications.updateChannel(
                Notifications.CHANNEL_ID_SEARCH,
                context.getString(R.string.search_notification_name),
            )
            setForeground(getForegroundInfo())
        }
        task = SearchTask(params.query, result = Files(params.type is Params.Text), id)
        cacheConfig = CacheConfig(params.asSu, thumbnailSize = context.resources.getDimensionPixelSize(R.dimen.thumbnail_size))
        return handleCancellation { work(params) }
    }

    private suspend fun <R> handleCancellation(action: suspend () -> R): R {
        return coroutineScope {
            val hook = launch {
                try {
                    while (true) delay(1000)
                } catch (e: CancellationException) {
                }
            }
            action()
                .also { hook.cancel() }
        }
    }

    private suspend fun work(params: Params): Result {
        val dataBuilder = Data.Builder()
        var job: Job? = null
        try {
            finderStore.addOrUpdate(task.cast())
            job = when (val type = params.type) {
                is Params.Text -> params.searchText(type)
                is Params.Names -> params.searchNames(type)
            }
            job.join()
            debugDelay(5)
            updateTask {
                toEnded()
            }
        } catch (e: CancellationException) {
            task = task.copy(state = SearchState.Stopped())
            finderStore {
                update(task.uuid, SearchState.Stopped())
            }
            dataBuilder.putBoolean(KEY_CANCELLED, true)
        } catch (e: Exception) {
            logE(e.toString())
            job?.join()
            task = task.copy(state = SearchState.Ended(), error = e.toString())
            finderStore {
                update(task.uuid, SearchState.Ended(), error = e.toString())
            }
            dataBuilder.putString(KEY_EXCEPTION, e.toString())
        } finally {
            context.ifCanNotice(::showNotification)
        }
        return Result.success(dataBuilder.build())
    }

    private fun NodeRef.toNode() = Node(this, rootId = task.uniqueId, content = NodeContent.Unknown)

    private fun Meta.toNode() = Node(NodeRef(path), rootId = task.uniqueId, content = NodeContent.Unknown, properties = toProperties())

    private fun TypedMeta.toNode() = meta.toNode().resolveType(mime)

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(hashCode(), foregroundNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }

    private fun showNotification() {
        val task = task
        val id = task.uniqueId
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, id, intent, UPDATING_FLAG)
        val icon = when {
            task.isStopped -> R.drawable.ic_notification_stopped
            task.error != null -> R.drawable.ic_notification_error
            else -> R.drawable.ic_notification_done
        }
        val titleId = when {
            task.error != null -> R.string.search_failed
            task.state is SearchState.Stopped -> R.string.search_stopped
            task.result.isEmpty -> R.string.search_empty
            else -> R.string.search_succeed
        }
        var (subText, text) = task.result.getCounters().takeIf { c -> c.any { it > 0 } }?.let { counters ->
            val subText = counters.joinToString(separator = " / ") { it.toString() }
            val text = when (counters.size) {
                3 -> context.getString(R.string.search_for_content_result, counters[0], counters[1], counters[2])
                2 -> context.getString(R.string.search_for_names_result, counters[0], counters[1])
                else -> null
            }
            subText to text
        } ?: (null to null)
        val error = task.error?.let { context.getString(R.string.search_error, it) }
        text = arrayOf(text, error).filterNotNull().joinToString(separator = ".\n")
        context.tryShow {
            val notification = NotificationCompat.Builder(context, Notifications.CHANNEL_ID_RESULT)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle(context.getString(titleId))
                .setSubText(subText)
                .apply { if (error != null) setStyle(NotificationCompat.BigTextStyle()) }
                .setContentText(text)
                .setSmallIcon(icon)
                .setColor(ContextCompat.getColor(context, R.color.day_night_primary))
                .setContentIntent(pendingIntent)
                .build()

            notification.flags = notification.flags or NotificationCompat.FLAG_AUTO_CANCEL

            notifications.updateChannel(
                Notifications.CHANNEL_ID_RESULT,
                context.getString(R.string.result_notification_name),
                NotificationManagerCompat.IMPORTANCE_DEFAULT,
            )
            notification to id
        }
    }

    private fun foregroundNotification(): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, Codes.FOREGROUND, intent, UPDATING_FLAG)
        notifications.updateChannel(
            Notifications.CHANNEL_ID_SEARCH,
            context.getString(R.string.search_notification_name),
            NotificationManagerCompat.IMPORTANCE_DEFAULT,
        )
        return NotificationCompat.Builder(context, Notifications.CHANNEL_ID_SEARCH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setContentTitle(context.getString(R.string.searching))
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.day_night_primary))
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun Params.refs() = targets.map { NodeRef(it) }
}