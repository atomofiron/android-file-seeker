package app.atomofiron.searchboxapp.screens.explorer

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.valueOrNull
import app.atomofiron.searchboxapp.custom.ExplorerView
import app.atomofiron.searchboxapp.di.dependencies.channel.CommonChannel
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.screens.common.delegates.StoragePermissionDelegate
import app.atomofiron.searchboxapp.screens.explorer.di.ExplorerInteractor
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerDockListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerItemActionListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.options.RootOptionAdapter.RootOptionListener
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerDockDelegate
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerItemActionListenerDelegate
import app.atomofiron.searchboxapp.screens.explorer.presenter.RootOptionDelegate
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.now
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExplorerScope
class ExplorerPresenter @Inject constructor(
    scope: CoroutineScope,
    private val viewState: ExplorerViewState,
    router: ExplorerRouter,
    private val storagePermissionDelegate: StoragePermissionDelegate,
    private val interactor: ExplorerInteractor,
    private val store: ExplorerStore,
    preferences: PreferenceStore,
    itemListener: ExplorerItemActionListenerDelegate,
    commonChannel: CommonChannel,
    dockDelegate: ExplorerDockDelegate,
    rootOptionDelegate: RootOptionDelegate,
) : BasePresenter<ExplorerViewModel, ExplorerRouter>(scope, router),
    ExplorerView.ExplorerViewOutput,
    RootAdapter.RootClickListener,
    ExplorerDockListener by dockDelegate,
    ExplorerItemActionListener by itemListener,
    RootOptionListener by rootOptionDelegate {

    private val folderVolumeUp by preferences.folderVolumeUp
    private val currentTab get() = viewState.currentTab.value

    init {
        commonChannel.appState.collect(scope) {
            if (it.started && it.rise) {
                interactor.updateRoots()
            }
        }
        var threshold = easterEggThreshold()
        scope.launch {
            while (true) {
                delay(threshold - now())
                if (now() > threshold) {
                    threshold = easterEggThreshold()
                    if (commonChannel.uiMode.valueOrNull?.isBlack == true) {
                        viewState.showEasterEgg.invoke()
                    }
                }
            }
        }
        commonChannel.userInteraction.collect(scope) {
            threshold = easterEggThreshold()
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
        // next time viewState.currentTab.value = viewState.tabs[index]
        interactor.setCurrentTab(currentTab)
    }

    fun onContinue() = interactor.setCurrentTab(currentTab)

    fun onVolumeUp(isCurrentDirVisible: Boolean): Boolean {
        val currentNode = viewState.deepest
            ?.takeIf { folderVolumeUp }
            ?: return false
        scrollOrOpenParent(currentNode, isCurrentDirVisible)
        return true
    }

    fun onBack(soft: Boolean, scrollToTop: () -> Boolean): Boolean = when {
        !soft -> super.onBack(false)
        else -> resetChecked() || scrollToTop() || unselectRoot()
    }

    override fun onCleared() {
        super.onCleared()
        interactor.drop(*viewState.tabs.toTypedArray())
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
        isTargetVisible -> interactor.toggleDir(currentTab, item.ref)
        else -> viewState.scrollTo(item)
    }

    private fun easterEggThreshold() = now() + Const.MINUTE
}