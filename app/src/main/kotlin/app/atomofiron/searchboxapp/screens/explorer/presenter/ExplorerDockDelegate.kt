package app.atomofiron.searchboxapp.screens.explorer.presenter

import android.Manifest.permission.POST_NOTIFICATIONS
import app.atomofiron.common.util.Alert
import app.atomofiron.common.util.extension.debugFailUnreachable
import app.atomofiron.common.util.flow.value
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.di.dependencies.router.FilePickingDelegate
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.common.delegates.copiable
import app.atomofiron.searchboxapp.screens.explorer.ExplorerRouter
import app.atomofiron.searchboxapp.screens.explorer.ExplorerScope
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewState
import app.atomofiron.searchboxapp.screens.explorer.di.ExplorerInteractor
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerDockListener
import javax.inject.Inject

@ExplorerScope
class ExplorerDockDelegate @Inject constructor(
    private val mode: ActivityMode,
    private val viewState: ExplorerViewState,
    private val router: ExplorerRouter,
    private val sharing: FilePickingDelegate,
    private val store: ExplorerStore,
    private val interactor: ExplorerInteractor,
) : ExplorerDockListener {

    override fun onSearchClick() = router.showFinder()

    override fun onSortPick(item: DockItem) {
        val sorting = NodeSorting(item.id)
            ?: return
        val root = viewState.currentTabFlow.value
            .roots.find { it.isSelected }
            ?: return
        val key = store.currentTabKey.value
        interactor.setSorting(key, root.id, sorting)
    }

    override fun onCopyClick() {
        val items = store.checked.value
            .takeIf { it.isNotEmpty() }
            ?: store.currentDeepest.value
                ?.let { listOf(it) }
            ?: return
        if (items.copiable()) {
            store.setForCopy(items)
            viewState.showAlert(Alert(R.string.copied))
        }
    }

    override fun onPasteClick(move: Boolean) {
        val key = viewState.currentTab.value
        val targets = store.pasteBuffer.value
        val dst = viewState.deepest ?: return
        interactor.copy(key, targets, dst, move)
        store.resetCopyBuffer()
    }

    override fun onCancelPastingClick() = store.resetCopyBuffer()

    override fun onSettingsClick() = router.showSettings()

    override fun onConfirmClick() {
        when (mode) {
            is ActivityMode.Default -> debugFailUnreachable()
            is ActivityMode.Receive -> receive(mode)
            is ActivityMode.Share -> share(mode.multiple)
        }
    }

    private fun receive(mode: ActivityMode.Receive) {
        val destination = store.currentDeepest.value?.ref
        destination ?: return
        router.permissions
            .request(POST_NOTIFICATIONS)
            .any {
                interactor.startReceive(destination, mode)
                router.finish()
            }
    }

    private fun share(multiple: Boolean) {
        val items = checkedFiles() ?: return
        when {
            multiple -> sharing.shareMultiplePicked(items)
            else -> sharing.shareSinglePicked(items.first())
        }
    }

    private fun checkedFiles() = store.checked.value
        .filter { it.isFile }
        .ifEmpty { null }
}