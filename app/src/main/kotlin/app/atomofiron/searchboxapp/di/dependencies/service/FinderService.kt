package app.atomofiron.searchboxapp.di.dependencies.service

import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.model.finder.SearchState
import app.atomofiron.searchboxapp.model.finder.GenericSearchTask
import app.atomofiron.searchboxapp.work.FinderWorker
import kotlinx.coroutines.CoroutineScope
import java.util.UUID

class FinderService(
    scope: CoroutineScope,
    private val workManager: WorkManager,
    private val notificationManager: NotificationManagerCompat,
    private val finderStore: FinderStore,
    private val preferenceStore: PreferenceStore,
    explorerStore: ExplorerStore,
) {
    init {
        workManager.cancelAllWork()
        explorerStore.removed.collect(scope) {
            finderStore.deleteResultFromTasks(it)
        }
    }

    fun search(query: String, where: List<NodeRef>, config: SearchOptions) {
        val maxSize = preferenceStore.maxFileSizeForSearch.value
        val maxDepth = preferenceStore.maxDepthForSearch.value
        val asSu = preferenceStore.asSu.value

        val targets = where.toTypedArray()
        val inputData = FinderWorker.inputData(query, asSu, config, maxSize, maxDepth, targets)
        val request = OneTimeWorkRequest.Builder(FinderWorker::class.java)
            .setInputData(inputData)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        workManager.beginWith(request).enqueue()
    }

    fun stop(uuid: UUID) {
        finderStore {
            update(uuid, SearchState.Stopping)
        }
        workManager.cancelWorkById(uuid)
    }

    fun drop(task: GenericSearchTask) {
        finderStore {
            drop(task)
        }
        notificationManager.cancel(task.uniqueId)
    }
}