package app.atomofiron.searchboxapp.screens.viewer

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.di.dependencies.interactor.TextViewerInteractor
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.textviewer.TextViewerSession
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.viewer.presenter.SearchAdapterPresenterDelegate
import app.atomofiron.searchboxapp.screens.viewer.presenter.TextViewerParams
import app.atomofiron.searchboxapp.screens.viewer.recycler.TextViewerAdapter
import kotlinx.coroutines.CoroutineScope

class TextViewerPresenter(
    params: TextViewerParams,
    scope: CoroutineScope,
    private val viewState: TextViewerViewState,
    router: TextViewerRouter,
    private val searchDelegate: SearchAdapterPresenterDelegate,
    private val interactor: TextViewerInteractor,
    session: TextViewerSession,
) : BasePresenter<TextViewerViewModel, TextViewerRouter>(scope, router),
    TextViewerAdapter.TextViewerListener,
    FinderAdapterOutput<SearchResult.Text> by searchDelegate
{

    private val itemRef: NodeRef get() = viewState.item.value.ref

    init {
        session.loading.collect(scope, viewState::setLoading)
        params.initialTaskId?.let { taskId ->
            interactor.fetchTask(itemRef, taskId) { task ->
                viewState.trySelectTask(task)
            }
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

    fun onPreviousClick() = onMoveClick(increment = false)

    fun onNextClick() = onMoveClick(increment = true)

    private fun onMoveClick(increment: Boolean) {
        val requiredLineIndex = viewState.changeCursor(increment)
        if (requiredLineIndex >= 0) {
            interactor.readFileToLine(itemRef, requiredLineIndex) {
                viewState.changeCursor(increment)
            }
        }
    }
}