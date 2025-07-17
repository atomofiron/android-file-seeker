package app.atomofiron.searchboxapp.screens.explorer

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.valueOrNull
import app.atomofiron.searchboxapp.custom.ExplorerView
import app.atomofiron.searchboxapp.injectable.channel.MainChannel
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerItemActionListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerItemActionListenerDelegate
import app.atomofiron.searchboxapp.screens.delegates.StoragePermissionDelegate
import kotlinx.coroutines.CoroutineScope

class ExplorerPresenter(
    scope: CoroutineScope,
    private val viewState: ExplorerViewState,
    router: ExplorerRouter,
    private val storagePermissionDelegate: StoragePermissionDelegate,
    private val explorerStore: ExplorerStore,
    private val explorerInteractor: ExplorerInteractor,
    itemListener: ExplorerItemActionListenerDelegate,
    mainChannel: MainChannel,
) : BasePresenter<ExplorerViewModel, ExplorerRouter>(scope, router),
    ExplorerView.ExplorerViewOutput,
    RootAdapter.RootClickListener,
    ExplorerItemActionListener by itemListener {

    private val currentTab get() = viewState.currentTab.value

    init {
        mainChannel.maximized.collect(scope) {
            explorerInteractor.updateRoots(currentTab)
        }
    }

    override fun onSubscribeData() = Unit

    override fun onRootClick(item: NodeRoot) {
        storagePermissionDelegate.request(
            granted = { explorerInteractor.toggleRoot(currentTab, item) },
            denied = { viewState.showPermissionRequiredWarning() }
        )
    }

    fun onAllowStorageClick() = storagePermissionDelegate.launchSettings()

    fun onSearchOptionSelected() = router.showFinder()

    fun onTabSelected(index: Int) {
        viewState.currentTab.value = when (index) {
            0 -> viewState.firstTab
            else -> viewState.secondTab
        }
        explorerInteractor.updateRoots(currentTab)
        explorerInteractor.updateCurrentTab(currentTab)
    }

    fun onSettingsOptionSelected() = router.showSettings()

    fun onVolumeUp(isCurrentDirVisible: Boolean) {
        val currentDir = viewState.getCurrentDir()
        currentDir ?: return
        scrollOrOpenParent(currentDir, isCurrentDirVisible)
    }

    override fun onBack(soft: Boolean): Boolean {
        if (soft) {
            val selectedRoot = viewState.firstTabItems
                .valueOrNull
                ?.roots
                ?.find { it.isSelected }
            selectedRoot ?: return false
            onRootClick(selectedRoot)
        }
        return soft || super.onBack(false)
    }

    private fun scrollOrOpenParent(item: Node, isTargetVisible: Boolean) = when {
        isTargetVisible -> explorerInteractor.toggleDir(currentTab, item)
        else -> viewState.scrollTo(item)
    }
}