package app.atomofiron.searchboxapp.screens.explorer.presenter

import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent.AndroidApp
import app.atomofiron.searchboxapp.screens.delegates.FileOperationsDelegate
import app.atomofiron.searchboxapp.screens.explorer.ExplorerRouter
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewState
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerItemActionListener

private val supportedOperations = listOf(R.id.menu_create, R.id.menu_rename, R.id.menu_clone, R.id.menu_delete, R.id.menu_copy_path, R.id.menu_share, R.id.menu_open_with, R.id.menu_apk, R.id.menu_launch, R.id.menu_install)

class ExplorerItemActionListenerDelegate(
    private val viewState: ExplorerViewState,
    private val operations: FileOperationsDelegate,
    private val menuListenerDelegate: ExplorerCurtainMenuDelegate,
    private val explorerStore: ExplorerStore,
    private val router: ExplorerRouter,
    private val interactor: ExplorerInteractor,
) : ExplorerItemActionListener {

    private val currentTab get() = viewState.currentTab.value

    override fun onItemLongClick(item: Node) {
        val files: List<Node> = when {
            item.isChecked -> explorerStore.checked.value
            else -> listOf(item)
        }
        val options = operations.operations(files, supportedOperations) ?: return
        menuListenerDelegate.showOptions(options)
    }

    override fun onItemClick(item: Node) = openItem(item)

    private fun openItem(item: Node) {
        when {
            item.isDirectory -> interactor.toggleDir(currentTab, item)
            item.content is AndroidApp -> operations.askForAndroidApp(item.content, currentTab)
            else -> router.showFile(item)
        }
    }

    override fun onItemCheck(item: Node, isChecked: Boolean) = interactor.checkItem(currentTab, item, isChecked)

    override fun onItemsBecomeVisible(items: List<Node>) = interactor.updateItems(currentTab, items)
}