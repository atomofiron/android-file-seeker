package app.atomofiron.searchboxapp.screens.result

import androidx.work.WorkManager
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.AlertErr
import app.atomofiron.common.util.extension.debugFailUnreachable
import app.atomofiron.common.util.extension.logE
import app.atomofiron.common.util.extension.mapCast
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.dependencies.db.dao.FinderDao
import app.atomofiron.searchboxapp.di.dependencies.router.FilePickingDelegate
import app.atomofiron.searchboxapp.di.dependencies.router.FileSharingDelegate
import app.atomofiron.searchboxapp.di.dependencies.router.startReceiveInto
import app.atomofiron.searchboxapp.di.dependencies.store.AppResources
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItem
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItemActionListener
import app.atomofiron.searchboxapp.screens.result.di.ResultInteractor
import app.atomofiron.searchboxapp.screens.result.presenter.ResultItemActionDelegate
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import app.atomofiron.searchboxapp.utils.formatDate
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@ResultScope
class ResultPresenter @Inject constructor(
    params: ResultPresenterParams,
    scope: CoroutineScope,
    private val viewState: ResultViewState,
    private val finderStore: FinderStore,
    private val interactor: ResultInteractor,
    router: ResultRouter,
    resources: AppResources,
    itemActionDelegate: ResultItemActionDelegate,
    private val picking: FilePickingDelegate,
    private val sharing: FileSharingDelegate,
    private val workManager: WorkManager,
    private val dao: FinderDao,
) : BasePresenter<ResultViewModel, ResultRouter>(scope, router),
    ResultItemActionListener by itemActionDelegate {

    private val taskId = params.taskId
    private val resources by resources

    init {
        if (finderStore.tasks.none { it.uniqueId == taskId }) {
            logE("No task found!")
            router.navigateBack()
        }
        onSubscribeData()
    }

    override fun onSubscribeData() = Unit

    fun onStopClick() = interactor.stop(viewState.taskUuid)

    fun onShareClick() {
        val checkedOnly = viewState.checked.isNotEmpty()
        val items = viewState.items.value.mapCast<_, ResultItem.Item, _> {
            item.takeIf { !checkedOnly || isChecked }
        }
        sharing.shareWith(items)
    }

    fun onExportClick() {
        val checkedOnly = viewState.checked.isNotEmpty()
        val data = when {
            checkedOnly -> viewState.result.toMarkdown {
                viewState.checked.contains(it.uniqueId)
            }
            else -> viewState.result.toMarkdown()
        }
        val title = "search_${resources.formatDate()}.md.txt";
        if (!router.shareFile(title, data)) {
            viewState.showAlert(AlertErr(R.string.no_activity))
        }
    }

    fun onConfirmClick() {
        val items = viewState.items.value.mapCast<_, ResultItem.Item, _> {
            item.takeIf { viewState.checked.contains(uniqueId) }
        }
        val mode = viewState.mode
        val first = items.firstOrNull() ?: return
        when {
            mode is ActivityMode.Default -> debugFailUnreachable()
            mode is ActivityMode.Receive -> {
                main {
                    workManager.startReceiveInto(first.ref, viewState.mode)
                    router.finish()
                }
            }
            mode !is ActivityMode.Share -> debugFailUnreachable()
            mode.multiple -> picking.shareMultiplePicked(items)
            else -> picking.shareSinglePicked(first)
        }
    }

    fun onSortingSelected(sorting: NodeSorting) {
        default {
            val new = finderStore.setSorting(taskId, sorting)
            new ?: return@default
            val cache = dao.get(taskId) ?: return@default
            dao.put(cache.copy(result = new))
        }
    }
}
