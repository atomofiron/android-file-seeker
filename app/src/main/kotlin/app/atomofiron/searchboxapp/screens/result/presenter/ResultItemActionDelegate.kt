package app.atomofiron.searchboxapp.screens.result.presenter

import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent.File
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.screens.delegates.FileOperationsDelegate
import app.atomofiron.searchboxapp.screens.result.ResultRouter
import app.atomofiron.searchboxapp.screens.result.ResultViewState
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItem
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItemActionListener

private val supportedOperations = listOf(R.id.menu_delete, R.id.menu_copy_path, R.id.menu_share, R.id.menu_open_with, R.id.menu_apk, R.id.menu_launch, R.id.menu_install)

class ResultItemActionDelegate(
    private val viewState: ResultViewState,
    private val operations: FileOperationsDelegate,
    private val router: ResultRouter,
    private val curtainDelegate: ResultCurtainMenuDelegate,
) : ResultItemActionListener {
    override fun onItemClick(item: Node) {
        when {
            item.isDirectory -> Unit // todo open dir
            item.content is File.Text -> router.openFile(item.path, viewState.task.value.uuid)
            item.content is File.AndroidApp -> operations.processApk(item)
            else -> router.openWith(item)
        }
    }

    override fun onItemLongClick(item: Node) = viewState.run {
        val matches = (task.value.result as SearchResult.FinderResult).matches
        val items = when {
            item.isChecked -> matches.mapNotNull { it.item.takeIf { checked.value.contains(it.uniqueId) } }
            else -> listOf(item)
        }
        val options = operations.operations(items, supported = supportedOperations) ?: return
        curtainDelegate.showOptions(options)
    }

    override fun onItemCheck(item: Node, isChecked: Boolean) {
        val checked = viewState.checked.value.toMutableList()
        when {
            isChecked -> checked.add(item.uniqueId)
            else -> checked.remove(item.uniqueId)
        }
        viewState.checked.value = checked
    }

    override fun onItemVisible(item: ResultItem.Item) = Unit
}