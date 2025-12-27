package app.atomofiron.searchboxapp.screens.explorer.presenter

import app.atomofiron.common.util.AlertErr
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.dependencies.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent.AndroidApp
import app.atomofiron.searchboxapp.screens.common.delegates.FileOperationsDelegate
import app.atomofiron.searchboxapp.screens.explorer.ExplorerRouter
import app.atomofiron.searchboxapp.screens.explorer.ExplorerScope
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewState
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerItemActionListener
import app.atomofiron.searchboxapp.utils.Rslt
import javax.inject.Inject

@ExplorerScope
class ExplorerItemActionListenerDelegate @Inject constructor(
    private val viewState: ExplorerViewState,
    private val operations: FileOperationsDelegate,
    private val menuListenerDelegate: ExplorerCurtainMenuDelegate,
    private val explorerStore: ExplorerStore,
    private val router: ExplorerRouter,
    private val interactor: ExplorerInteractor,
) : ExplorerItemActionListener {

    private val currentTab get() = viewState.currentTab.value

    override fun onItemLongClick(item: Node) {
        val nodes: List<Node> = if (item.isChecked) {
            val checked = explorerStore.checked.value
            explorerStore.currentItems.filter { node ->
                checked.any { node.ref == it.ref }
            }
        } else {
            listOf(item)
        }
        val options = operations.operations(nodes)
        when (options) {
            is Rslt.Ok -> menuListenerDelegate.showOptions(options.value)
            is Rslt.Err -> when {
                options.isEmpty -> AlertErr(R.string.unknown_error)
                else -> AlertErr(options.message)
            }.let { viewState.showAlert(it) }
        }
    }

    override fun onItemClick(item: Node) = openItem(item)

    private fun openItem(item: Node) {
        when {
            item.isDirectory -> interactor.toggleDir(currentTab, item)
            item.content is AndroidApp -> operations.askForAndroidApp(item.content, currentTab)
            else -> router.showFile(item)
        }
    }

    override fun onItemCheck(item: Node, toChecked: Boolean): Boolean {
        if (item.isOpened && toChecked) {
            val checked = explorerStore.checked.value
                .filter { it.ref.isChildOf(item.ref) }
            if (checked.isEmpty()) {
                interactor.check(currentTab, item, true)
            } else {
                interactor.check(currentTab, checked, false)
                return false
            }
        } else if (item.isOpened && !toChecked) {
            interactor.check(currentTab, item, false)
            item.children?.let {
                interactor.check(currentTab, it, true)
            }
        } else {
            interactor.check(currentTab, item, toChecked)
            val checked = explorerStore.checked.value
                .filter { item.ref.isChildOf(it.ref) }
            interactor.check(currentTab, checked, false)
        }
        return true
    }

    override fun onItemsBecomeVisible(items: List<Node>) = interactor.updateItems(currentTab, items)
}