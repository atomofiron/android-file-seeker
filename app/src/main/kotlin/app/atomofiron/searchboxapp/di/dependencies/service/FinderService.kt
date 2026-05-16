package app.atomofiron.searchboxapp.di.dependencies.service

import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import app.atomofiron.common.util.extension.invoke
import app.atomofiron.common.util.extension.launchOnDefault
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.db.dao.FinderDao
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.GenericSearchTask
import app.atomofiron.searchboxapp.model.finder.GlobalSearchTask
import app.atomofiron.searchboxapp.model.finder.QueryParams
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.model.finder.SearchStatus
import app.atomofiron.searchboxapp.utils.CoroutineLauncher
import app.atomofiron.searchboxapp.work.FinderWorker
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinderService @Inject constructor(
    scope: AppScope,
    private val workManager: WorkManager,
    private val notificationManager: NotificationManagerCompat,
    private val store: FinderStore,
    private val preferenceStore: PreferenceStore,
    explorerStore: ExplorerStore,
    dao: FinderDao,
) : CoroutineLauncher by CoroutineLauncher(scope) {

    init {
        workManager.cancelAllWork()
        explorerStore.deleted.collect(scope) {
            store.deleteResultFromTasks(it)
        }
        scope.launchOnDefault {
            val cached = dao.all().map {
                GlobalSearchTask(it.params, it.result, uniqueId = it.id, status = SearchStatus.Ended(stopped = it.stopped), cached = true)
            }
            store.addAll(cached)
        }
    }

    fun search(query: String, where: List<NodeRef>, config: SearchOptions) {
        val maxSize = preferenceStore.maxFileSizeForSearch.value
        val maxDepth = preferenceStore.maxDepthForSearch.value
        val asSu = preferenceStore.asSu.value
        val query = QueryParams(query, regex = config.regex, ignoreCase = config.ignoreCase)
        val type = when {
            config.contentSearch -> FinderWorker.Params.Text(maxSize = maxSize)
            else -> FinderWorker.Params.Names(excludeDirs = config.excludeDirs)
        }
        val params = FinderWorker.Params(query, type, maxDepth = maxDepth, targets = where.map { it.bytes }, asSu = asSu)
        val request = OneTimeWorkRequest.Builder(FinderWorker::class.java)
            .setInputData(Data(params))
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        workManager.beginWith(request).enqueue()
    }

    fun stop(uuid: UUID) {
        workManager.cancelWorkById(uuid)
    }

    fun drop(task: GenericSearchTask) {
        default {
            store.drop(task.uuid)
        }
        notificationManager.cancel(task.uniqueId)
    }
}