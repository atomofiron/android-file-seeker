package app.atomofiron.searchboxapp.screens.viewer.presenter

import app.atomofiron.common.arch.Recipient
import app.atomofiron.common.util.extension.launchOnIO
import app.atomofiron.common.util.extension.withMain
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.finder.QueryParams
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.finder.SearchTask
import app.atomofiron.searchboxapp.model.textviewer.toLocal
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.screens.viewer.TextViewerRouter
import app.atomofiron.searchboxapp.screens.viewer.TextViewerViewState
import app.atomofiron.searchboxapp.screens.viewer.di.TextViewerInteractor
import app.atomofiron.searchboxapp.screens.viewer.presenter.curtain.CurtainSearchDelegate
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toNodeError
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.toUni
import kotlinx.coroutines.CoroutineScope

class SearchAdapterPresenterDelegate(
    private val scope: CoroutineScope,
    private val viewState: TextViewerViewState,
    private val router: TextViewerRouter,
    private val interactor: TextViewerInteractor,
    private val preferences: PreferenceStore,
    curtainChannel: CurtainChannel,
) : Recipient, FinderAdapterOutput<SearchResult.Local> {

    private val curtain = CurtainSearchDelegate(this, viewState, scope)

    init {
        curtainChannel.flow.collectForMe(scope) { controller ->
            curtain.setController(controller)
        }
    }

    fun show() = router.showCurtain(recipient, R.layout.curtain_text_viewer_search)

    override fun onOptionsChange(options: SearchOptions) {
        preferences { setLocalSearchOptions(options.toLocal()) }
    }

    override fun onConfigVisibilityClick() = Unit

    override fun onHistoryClick() = Unit

    override fun onCharacterClick(value: String) = viewState.sendInsertInQuery(value)

    override fun onSearchChange(value: String) = viewState.updateSearchQuery(value)

    override fun onSearchClick(value: String) {
        val config = viewState.toggles.value
        val params = QueryParams(value, regex = config.regex, ignoreCase = config.ignoreCase)
        interactor.search(viewState.item.value.ref, params)
    }

    override fun onEditCharacters(new: List<String>) = preferences { setSpecialCharacters(new.toTypedArray()) }

    override fun onEditMaxDepth(new: Int) = Unit

    override fun onItemClick(item: FinderStateItem.Task<SearchResult.Local>) = trySelectTask(item.task)

    override fun onProgressRemoveClick(item: FinderStateItem.Task<SearchResult.Local>) {
        interactor.removeTask(viewState.item.value.ref, item.task.uniqueId)
        viewState.dropTask()
    }

    override fun onReplaceClick(value: String) = Unit

    override fun onProgressStopClick(item: FinderStateItem.Task<SearchResult.Local>) = Unit

    override fun onTestTextChange(value: String?) = Unit

    override fun onEditMaxSize(new: Long) = Unit

    fun trySelectTask(task: SearchTask<SearchResult.Local>) {
        val hash = task.result.hash
        when {
            hash != null -> scope.launchOnIO {
                val result = interactor.getHash(hash.ref)
                withMain {
                    when {
                        task.error != null -> task.error.show()
                        hash.hash == result.ok()?.value -> task.trySelect()
                        result is Rslt.Err -> result.message.toNodeError().show()
                        else -> NodeError.FileWasChanged.show()
                    }
                }
            }
            else -> task.trySelect()
        }
    }

    private fun SearchTask<SearchResult.Local>.trySelect() {
        if (viewState.trySelectTask(this)) {
            curtain.controller?.close()
        }
    }

    private fun NodeError.show() {
        curtain.controller?.showSnackbar(toUni())
            ?: viewState.showError(this)
    }
}