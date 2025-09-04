package app.atomofiron.searchboxapp.screens.explorer

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.valueOrNull
import app.atomofiron.searchboxapp.custom.ExplorerView
import app.atomofiron.searchboxapp.di.dependencies.channel.MainChannel
import app.atomofiron.searchboxapp.di.dependencies.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerItemActionListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerItemActionListenerDelegate
import app.atomofiron.searchboxapp.screens.common.delegates.StoragePermissionDelegate
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerDockListener
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerDockDelegate
import kotlinx.coroutines.CoroutineScope

class ExplorerPresenter(
    scope: CoroutineScope,
    private val viewState: ExplorerViewState,
    router: ExplorerRouter,
    private val storagePermissionDelegate: StoragePermissionDelegate,
    private val interactor: ExplorerInteractor,
    private val store: ExplorerStore,
    itemListener: ExplorerItemActionListenerDelegate,
    mainChannel: MainChannel,
    dockDelegate: ExplorerDockDelegate,
) : BasePresenter<ExplorerViewModel, ExplorerRouter>(scope, router),
    ExplorerView.ExplorerViewOutput,
    RootAdapter.RootClickListener,
    ExplorerDockListener by dockDelegate,
    ExplorerItemActionListener by itemListener {

    private val currentTab get() = viewState.currentTab.value

    init {
        mainChannel.maximized.collect(scope) {
            interactor.updateRoots(currentTab)
        }
    }

    override fun onSubscribeData() = Unit

    override fun onRootClick(item: NodeRoot) {
        storagePermissionDelegate.request(
            granted = { interactor.toggleRoot(currentTab, item) },
            denied = { viewState.showPermissionRequiredWarning() }
        )
    }

    fun onAllowStorageClick() = storagePermissionDelegate.launchSettings()

    fun onTabSelected(index: Int) {
        viewState.currentTab.value = viewState.tabs[index]
        interactor.updateRoots(currentTab)
        interactor.updateCurrentTab(currentTab)
    }

    fun onVolumeUp(isCurrentDirVisible: Boolean) {
        val currentNode = viewState.currentNode
        currentNode ?: return
        scrollOrOpenParent(currentNode, isCurrentDirVisible)
    }

    fun onBack(soft: Boolean, scrollToTop: () -> Boolean): Boolean = when {
        !soft -> super.onBack(false)
        else -> resetChecked() || scrollToTop() || unselectRoot()
    }

    private fun resetChecked(): Boolean {
        return store.checked.value.isNotEmpty().also {
            if (it) interactor.resetChecked(viewState.currentTab.value)
        }
    }

    private fun unselectRoot(): Boolean {
        return null != viewState.currentTabFlow
            .valueOrNull
            ?.roots
            ?.find { it.isSelected }
            ?.let { interactor.toggleRoot(currentTab, it) }
    }

    private fun scrollOrOpenParent(item: Node, isTargetVisible: Boolean) = when {
        isTargetVisible -> interactor.toggleDir(currentTab, item)
        else -> viewState.scrollTo(item)
    }
}