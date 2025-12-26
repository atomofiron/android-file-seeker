package app.atomofiron.searchboxapp.screens.viewer

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.extension.launchOnIO
import app.atomofiron.common.util.extension.logE
import app.atomofiron.common.util.extension.withMain
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.LocalSearchResult
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.viewer.di.TextViewerInteractor
import app.atomofiron.searchboxapp.screens.viewer.presenter.SearchAdapterPresenterDelegate
import app.atomofiron.searchboxapp.screens.viewer.presenter.TextViewerParams
import app.atomofiron.searchboxapp.screens.viewer.recycler.TextViewerAdapter
import app.atomofiron.searchboxapp.screens.viewer.state.CursorResult
import kotlinx.coroutines.CoroutineScope

class TextViewerPresenter(
    params: TextViewerParams,
    scope: CoroutineScope,
    private val viewState: TextViewerViewState,
    router: TextViewerRouter,
    private val searchDelegate: SearchAdapterPresenterDelegate,
    private val interactor: TextViewerInteractor,
    session: TextViewerSession?,
) : BasePresenter<TextViewerViewModel, TextViewerRouter>(scope, router),
    TextViewerAdapter.TextViewerListener,
    FinderAdapterOutput<LocalSearchResult> by searchDelegate
{

    private val itemRef: NodeRef get() = viewState.item.value.ref

    init {
        session?.loading?.collect(scope, viewState::setLoading)
        scope.launchOnIO {
            session ?: return@launchOnIO withMain {
                router.navigateBack()
            }
            val item = interactor.fetchItem(itemRef)
            session.updateItem(item)

            params.initialTaskId
                ?.let { interactor.fetchTask(itemRef, it) }
                ?.let { searchDelegate.trySelectTask(it) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        interactor.closeSession(itemRef)
    }

    override fun onSubscribeData() = Unit

    override fun onLineVisible(index: Int) = interactor.readFileToLine(itemRef, index)

    override fun onNavigationClick(): Boolean = when (viewState.currentTask.value) {
        null -> super.onNavigationClick()
        else -> true.also { viewState.dropTask() }
    }

    override fun onBack(soft: Boolean): Boolean {
        return (viewState.currentTask.value != null).also {
            if (it) viewState.dropTask()
        }
    }

    fun onSearchClick() = searchDelegate.show()

    fun onPreviousClick() = onMoveClick(forward = false)

    fun onNextClick() = onMoveClick(forward = true)

    fun onCopyPathClick() = interactor.copy(viewState.item.value)

    private fun onMoveClick(forward: Boolean) {
        when (val result = viewState.switchCursor(forward)) {
            is CursorResult.Ok -> Unit
            is CursorResult.Err -> logE(result.message)
            is CursorResult.Load -> interactor.readFileToLine(itemRef, result.line) {
                viewState.switchCursor(forward)
            }
        }
    }
}