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
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItem
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItemActionListener
import app.atomofiron.searchboxapp.screens.result.di.ResultInteractor
import app.atomofiron.searchboxapp.screens.result.presenter.ResultItemActionDelegate
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import app.atomofiron.searchboxapp.utils.ExplorerUtils.resolveContent
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toNode
import app.atomofiron.searchboxapp.utils.ExplorerUtils.update
import app.atomofiron.searchboxapp.utils.formatDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.mapNotNull
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
    preferences: PreferenceStore,
    private val dao: FinderDao,
) : BasePresenter<ResultViewModel, ResultRouter>(scope, router),
    ResultItemActionListener by itemActionDelegate {

    private val taskId = params.taskId
    private val resources by resources
    private val asSu by preferences.asSu

    init {
        if (finderStore.tasks.none { it.uniqueId == taskId }) {
            logE("No task found!")
            router.navigateBack()
        }
        onSubscribeData()
    }

    override fun onSubscribeData() {
        val tasks = finderStore.tasksFlow.mapNotNull { tasks ->
            tasks.find { it.uniqueId == taskId }
        }
        combineTransform<_, _, Unit>(tasks, viewState.checked) { task, checked ->
            val result = task.result
            val cached = result.matches.mapNotNull { match ->
                val cached = viewState.cache[match.uniqueId]
                when {
                    cached == null -> {
                        val content = match.ref.resolveContent(match.hash.mime, match.hash.meta)
                        val item = match.ref.toNode(rootId = taskId, meta = match.hash.meta, content = content)
                        ResultItem.Item(match = match, item).also {
                            cacheAsync(it)
                        }
                    }
                    cached.match != match -> cached.copy(match = match)
                    else -> null
                }
            }
            viewState.cache(cached)
            viewState.reduce(task, checked)
        }.run {
            default { collect() }
        }
    }

    private fun cacheAsync(item: ResultItem.Item) {
        io {
            val new = item.copy(item = item.item.update(asSu))
            viewState.cache(new)
        }
    }

    fun onStopClick() = interactor.stop(viewState.taskUuid)

    fun onShareClick() {
        val checkedOnly = viewState.checked.value.isNotEmpty()
        val items = viewState.items.value.mapCast<_, ResultItem.Item, _> {
            item.takeIf { !checkedOnly || isChecked }
        }
        sharing.shareWith(items)
    }

    fun onExportClick() {
        val checked = viewState.checked.value
        val checkedOnly = checked.isNotEmpty()
        val data = when {
            checkedOnly -> viewState.result.toMarkdown {
                checked.contains(it.uniqueId)
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
            item.takeIf { viewState.checked.value.contains(uniqueId) }
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
