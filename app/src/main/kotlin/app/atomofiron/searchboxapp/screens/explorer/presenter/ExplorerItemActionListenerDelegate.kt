package app.atomofiron.searchboxapp.screens.explorer.presenter

import app.atomofiron.searchboxapp.injectable.interactor.ApkInteractor
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent.File.Apk
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.explorer.ExplorerRouter
import app.atomofiron.searchboxapp.screens.explorer.ExplorerViewState
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerItemActionListener

class ExplorerItemActionListenerDelegate(
    private val viewState: ExplorerViewState,
    private val menuListenerDelegate: ExplorerCurtainMenuDelegate,
    private val explorerStore: ExplorerStore,
    private val router: ExplorerRouter,
    private val explorerInteractor: ExplorerInteractor,
    private val apkInteractor: ApkInteractor,
) : ExplorerItemActionListener {

    private val currentTab get() = viewState.currentTab.value

    override fun onItemLongClick(item: Node) {
        val files: List<Node> = when {
            item.isChecked -> explorerStore.searchTargets.value
            else -> listOf(item)
        }
        val ids = when {
            files.size > 1 -> viewState.manyFilesOptions
            files.first().isRoot -> viewState.rootOptions
            files.first().isDirectory -> viewState.directoryOptions
            else -> viewState.oneFileOptions
        }
        val options = ExplorerItemOptions(ids, files, viewState.itemComposition.value)
        menuListenerDelegate.showOptions(options)
    }

    override fun onItemClick(item: Node) = openItem(item)

    private fun openItem(item: Node) {
        when {
            item.isDirectory -> explorerInteractor.toggleDir(currentTab, item)
            item.content is Apk -> apkInteractor.installApk(viewState.currentTab.value, item)
            item.isFile -> router.showFile(item)
        }
    }

    override fun onItemCheck(item: Node, isChecked: Boolean) = explorerInteractor.checkItem(currentTab, item, isChecked)

    override fun onItemVisible(item: Node) = explorerInteractor.updateItem(currentTab, item)
}