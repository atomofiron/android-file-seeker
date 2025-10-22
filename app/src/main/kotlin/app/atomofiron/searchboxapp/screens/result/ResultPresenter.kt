package app.atomofiron.searchboxapp.screens.result

import androidx.work.WorkManager
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.AlertMessage
import app.atomofiron.common.util.Unreachable
import app.atomofiron.common.util.extension.logE
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.dependencies.interactor.ResultInteractor
import app.atomofiron.searchboxapp.di.dependencies.router.FilePickingDelegate
import app.atomofiron.searchboxapp.di.dependencies.router.startReceiveInto
import app.atomofiron.searchboxapp.di.dependencies.store.AppResources
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItemActionListener
import app.atomofiron.searchboxapp.screens.result.presenter.ResultItemActionDelegate
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import app.atomofiron.searchboxapp.utils.formatDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.collections.ifEmpty

class ResultPresenter(
    params: ResultPresenterParams,
    scope: CoroutineScope,
    private val viewState: ResultViewState,
    private val finderStore: FinderStore,
    private val interactor: ResultInteractor,
    router: ResultRouter,
    resources: AppResources,
    itemActionDelegate: ResultItemActionDelegate,
    private val sharing: FilePickingDelegate,
    private val workManager: WorkManager,
) : BasePresenter<ResultViewModel, ResultRouter>(scope, router),
    ResultItemActionListener by itemActionDelegate {
    private val taskId = params.taskId
    private val resources by resources

    init {
        if (!finderStore.tasks.any { it.uniqueId == taskId }) {
            logE("No task found!")
            router.navigateBack()
        }
        onSubscribeData()
    }

    override fun onSubscribeData() = Unit

    fun onStopClick() = interactor.stop(viewState.taskUuid)

    fun onShareClick() {
        val result = viewState.result.value
        val checkedOnly = result.matches.any { it.item.isChecked }
        val items = result.matches.mapNotNull { match ->
            match.item.takeIf { !checkedOnly || it.isChecked }
        }
        router.shareWith(items)
    }

    fun onExportClick() {
        val result = viewState.result.value
        val checkedOnly = result.matches.any { it.item.isChecked }
        val data = result.toMarkdown(checkedOnly)
        val title = "search_${resources.formatDate()}.md.txt";
        if (!router.shareFile(title, data)) {
            viewState.showAlert(AlertMessage(R.string.no_activity, important = true))
        }
    }

    fun onConfirmClick() {
        val matches = viewState.result.value.matches
        val items = viewState.checked.value
            .mapNotNull { uniqueId ->
                matches.find { it.item.uniqueId == uniqueId }?.item
            }.ifEmpty { return }
        val mode = viewState.mode
        val first = items.firstOrNull() ?: return
        when {
            mode is ActivityMode.Default -> Unreachable
            mode is ActivityMode.Receive -> {
                scope.launch {
                    workManager.startReceiveInto(first.ref, viewState.mode)
                    router.finish()
                }
            }
            mode !is ActivityMode.Share -> Unreachable
            mode.multiple -> sharing.shareMultiplePicked(items)
            else -> sharing.shareSinglePicked(first)
        }
    }

    fun onSortingSelected(sorting: NodeSorting) {
        scope.launch {
            finderStore.setSorting(taskId, sorting)
        }
    }
}
