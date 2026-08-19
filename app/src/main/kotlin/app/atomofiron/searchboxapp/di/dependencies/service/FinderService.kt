package app.atomofiron.searchboxapp.di.dependencies.service

import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import app.atomofiron.common.util.extension.invoke
import app.atomofiron.common.util.extension.launchOnDefault
import app.atomofiron.common.util.extension.withIO
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.db.dao.FinderDao
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.GenericSearchTask
import app.atomofiron.searchboxapp.model.finder.GlobalSearchResult
import app.atomofiron.searchboxapp.model.finder.GlobalSearchTask
import app.atomofiron.searchboxapp.model.finder.QueryParams
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.model.finder.SearchResultCache
import app.atomofiron.searchboxapp.model.finder.SearchStatus
import app.atomofiron.searchboxapp.work.FinderWorker
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.Uuid
import kotlin.uuid.toJavaUuid

@Singleton
class FinderService @Inject constructor(
    scope: AppScope,
    private val workManager: WorkManager,
    private val notificationManager: NotificationManagerCompat,
    private val store: FinderStore,
    private val preferenceStore: PreferenceStore,
    private val dao: FinderDao,
    explorerStore: ExplorerStore,
) {

    init {
        workManager.cancelAllWork()
        explorerStore.deleted.collect(scope) {
            store.deleteResultFromTasks(it)
        }
        scope.launchOnDefault {
            val cached = dao.all().mapNotNull {
                val result = dao.read(it.id)
                    ?: return@mapNotNull null
                GlobalSearchTask(query = it.params, result = result, uniqueId = it.id, status = SearchStatus.Ended(stopped = it.stopped), cached = true)
            }
            store.addAll(cached)
        }
    }

    suspend fun search(query: String, where: List<NodeRef>, config: SearchOptions) = withIO {
        val maxSize = preferenceStore.maxFileSizeForSearch.value
        val maxDepth = preferenceStore.maxDepthForSearch.value
        val asSu = preferenceStore.asSu.value
        val query = QueryParams(query, regex = config.regex, ignoreCase = config.ignoreCase)
        val type = when {
            config.contentSearch -> FinderWorker.Params.Text(maxSize = maxSize.resolve())
            else -> FinderWorker.Params.Names(excludeDirs = config.excludeDirs)
        }
        val result = GlobalSearchResult(forText = config.contentSearch)
        val cache = SearchResultCache(stopped = false, params = query)
        val uniqueId = dao.store(cache, result)
        val params = FinderWorker.Params(uniqueId, query, type, maxDepth = maxDepth, targets = where.map { it.bytes }, asSu = asSu)
        val request = OneTimeWorkRequest.Builder(FinderWorker::class.java)
            .setInputData(Data(params))
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        workManager.beginWith(request).enqueue()
    }

    fun stop(uuid: Uuid) {
        workManager.cancelWorkById(uuid.toJavaUuid())
    }

    suspend fun drop(task: GenericSearchTask) {
        store.drop(task.uuid)
        notificationManager.cancel(task.uniqueId)
    }
}