package app.atomofiron.searchboxapp.screens.explorer.presenter

import app.atomofiron.searchboxapp.di.dependencies.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent.AndroidApp
import app.atomofiron.searchboxapp.screens.common.delegates.FileOperationsDelegate
import app.atomofiron.searchboxapp.screens.explorer.ExplorerRouter
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewState
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerItemActionListener

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
        val options = operations.operations(files) ?: return
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