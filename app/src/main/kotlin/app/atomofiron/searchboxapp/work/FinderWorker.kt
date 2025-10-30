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
import app.atomofiron.common.util.extension.invoke
import app.atomofiron.common.util.extension.logE
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
import app.atomofiron.searchboxapp.model.finder.ItemMatch
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.model.finder.SearchParams
import app.atomofiron.searchboxapp.model.finder.SearchResult.Files
import app.atomofiron.searchboxapp.model.finder.SearchState
import app.atomofiron.searchboxapp.model.finder.SearchTask
import app.atomofiron.searchboxapp.model.finder.GenericSearchTask
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch
import app.atomofiron.searchboxapp.poop
import app.atomofiron.searchboxapp.screens.main.MainActivity
import app.atomofiron.searchboxapp.utils.Codes
import app.atomofiron.searchboxapp.utils.ExplorerUtils.resolveType
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toProperties
import app.atomofiron.searchboxapp.utils.ExplorerUtils.update
import app.atomofiron.searchboxapp.utils.canForegroundService
import app.atomofiron.searchboxapp.utils.ifCanNotice
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import uniffi.native_lib.Meta
import uniffi.native_lib.NameSearchProgress
import uniffi.native_lib.SimpleResult
import uniffi.native_lib.TextSearchProgress
import uniffi.native_lib.TypedMeta
import java.util.regex.Pattern
import javax.inject.Inject

class FinderWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {
    companion object {
        @SuppressLint("InlinedApi")
        private const val UPDATING_FLAG = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        private const val UNDEFINED = -1

        private const val KEY_EXCEPTION = "KEY_EXCEPTION"
        private const val KEY_CANCELLED = "KEY_CANCELLED"

        private const val KEY_QUERY = "KEY_QUERY"
        private const val KEY_USE_SU = "KEY_USE_SU"
        private const val KEY_USE_REGEX = "KEY_USE_REGEX"
        private const val KEY_MAX_SIZE = "KEY_MAX_SIZE"
        private const val KEY_CASE_INSENSITIVE = "KEY_CASE_INSENSITIVE"
        private const val KEY_EXCLUDE_DIRS = "KEY_EXCLUDE_DIRS"
        private const val KEY_FOR_CONTENT = "KEY_FOR_CONTENT"
        private const val KEY_MAX_DEPTH = "KEY_MAX_DEPTH"
        private const val KEY_WHERE_PATH = "KEY_WHERE_PATH_"

        fun inputData(query: String, asSu: Boolean, config: SearchOptions, maxSize: Int, maxDepth: Int, targets: Array<NodeRef>) = Data.Builder()
            .putString(KEY_QUERY, query)
            .putBoolean(KEY_USE_SU, asSu)
            .putBoolean(KEY_USE_REGEX, config.useRegex)
            .putInt(KEY_MAX_SIZE, maxSize)
            .putBoolean(KEY_CASE_INSENSITIVE, config.ignoreCase)
            .putBoolean(KEY_EXCLUDE_DIRS, config.excludeDirs)
            .putBoolean(KEY_FOR_CONTENT, config.contentSearch)
            .putInt(KEY_MAX_DEPTH, maxDepth)
            .apply {
                for (i in targets.indices) putByteArray("$KEY_WHERE_PATH$i", targets[i].bytes)
            }
            .build()
    }
    private val asSu = inputData.getBoolean(KEY_USE_SU, false)
    private val useRegex = inputData.getBoolean(KEY_USE_REGEX, false)
    private val query: String = inputData.getString(KEY_QUERY) ?: ""
    private lateinit var pattern: Pattern
    private val maxSize = inputData.getLong(KEY_MAX_SIZE, 0L)
    private val ignoreCase = inputData.getBoolean(KEY_CASE_INSENSITIVE, false)
    private val excludeDirs = inputData.getBoolean(KEY_EXCLUDE_DIRS, false)
    private val forContent = inputData.getBoolean(KEY_FOR_CONTENT, false)
    private val maxDepth = inputData.getInt(KEY_MAX_DEPTH, UNDEFINED)
    private val params = SearchParams(query, useRegex = useRegex, ignoreCase = ignoreCase)

    private val taskMutex = Mutex()
    private var task: GenericSearchTask = SearchTask(
        SearchParams(query, useRegex, ignoreCase),
        Files(forContent),
        id,
    )
    private var process: Process? = null
    private val cacheConfig = CacheConfig(asSu, thumbnailSize = context.resources.getDimensionPixelSize(R.dimen.thumbnail_size))
    private val progressJobs = mutableListOf<Job>()

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
        if (useRegex && !forContent) {
            val flags = if (ignoreCase) Pattern.CASE_INSENSITIVE else 0
            pattern = Pattern.compile(query, flags)
        }
        DaggerInjector.appComponent.inject(this)
    }

    private fun searchText(where: List<Node>) {
        if (isStopped) {
            return
        }
        val targets = where.map { it.ref }
        appScope.launch {
            val result = NativeBridge.findText(params, targets, maxDepth = maxDepth, maxSize = maxSize, asSu) { match ->
                when (match) {
                    is TextSearchProgress.Ok -> {
                        poop("match $match")
                        val lineIndex = match.line?.toInt() ?: return@findText
                        val result = task.result as Files
                        val itemMatch = result.matches.find { it.item.ref.theSame(match.path) }
                            ?.let { it as ItemMatch.Multiply }
                            ?: NodeRef(match.path).let {
                                val node = NativeBridge.type(it, asSu).value?.toNode() ?: it.toNode()
                                ItemMatch.Multiply(node, 0, mutableMapOf())
                            }
                        val matchesMap = itemMatch.matchesMap as MutableMap<Int, MutableList<TextLineMatch>>
                        val matches = matchesMap.getOrPut(lineIndex) { mutableListOf() }
                        matches.add(TextLineMatch(match.offset.toLong(), match.length.toInt()))
                        appScope {
                            addToResult(itemMatch.copy(count = itemMatch.count.inc()))
                        }
                    }
                    is TextSearchProgress.Err -> Unit.also { poop("err $match") } // todo
                }
            }
            poop("result $result")
            updateTask {
                val error = (result as? SimpleResult.Err)?.v1
                toEnded(error = error)
            }
        }.let { progressJobs.add(it) }
    }

    private suspend fun waitForJobs() = progressJobs.forEach { it.join() }

    private suspend inline fun updateTask(transformation: GenericSearchTask.() -> GenericSearchTask) {
        taskMutex.withLock {
            task = task.transformation()
        }
        finderStore.addOrUpdate(task)
    }

    private fun searchNames(where: List<Node>) {
        if (isStopped) {
            return
        }
        val targets = where.map { it.ref }
        appScope {
            NativeBridge.findNames(params, targets, maxDepth, excludeDirs, asSu) {
                appScope {
                    when (it) {
                        is NameSearchProgress.Ok -> addToResult(ItemMatch.Single(it.v1.toNode()))
                        is NameSearchProgress.Err -> Unit // todo
                    }
                }
            }
        }.let { progressJobs.add(it) }
    }

    // todo remove deleting files from results
    private suspend fun addToResult(itemMatch: ItemMatch) {
        updateTask {
            val result = task.result as Files
            copyWith(result.add(itemMatch))
        }
    }

    override suspend fun doWork(): Result {
        if (query.isEmpty()) {
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
        return handleCancellation(::work)
    }

    private suspend fun <R> handleCancellation(action: suspend () -> R): R {
        return coroutineScope {
            val hook = launch {
                try {
                    while (true) delay(1000)
                } catch (e: CancellationException) {
                    process?.destroy()
                }
            }
            action()
                .also { hook.cancel() }
        }
    }

    private suspend fun work(): Result {
        val dataBuilder = Data.Builder()
        try {
            finderStore.addOrUpdate(task)
            val targets = mutableListOf<Node>()
            for (i in 0..Int.MAX_VALUE) {
                val path = inputData.getByteArray("$KEY_WHERE_PATH$i")
                path ?: break
                targets.add(Node(NodeRef(path), content = NodeContent.Unknown).update(cacheConfig))
            }
            when {
                forContent -> searchText(targets)
                else -> searchNames(targets)
            }
            waitForJobs()
            debugDelay(5)
            updateTask {
                toEnded()
            }
        } catch (e: CancellationException) {
            task = task.copy(state = SearchState.Stopped())
            finderStore {
                update(task.uuid, SearchState.Stopped())
            }
            process?.destroy()
            dataBuilder.putBoolean(KEY_CANCELLED, true)
        } catch (e: Exception) {
            logE(e.toString())
            waitForJobs()
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

    private fun newNode(path: String) = Node(NodeRef(path), rootId = task.uniqueId, content = NodeContent.Unknown).update(cacheConfig)

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
}