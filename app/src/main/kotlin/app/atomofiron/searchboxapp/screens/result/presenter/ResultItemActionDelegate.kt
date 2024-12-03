package app.atomofiron.searchboxapp.screens.result.presenter

import app.atomofiron.searchboxapp.injectable.interactor.ResultInteractor
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.result.ResultRouter
import app.atomofiron.searchboxapp.screens.result.ResultViewState
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItem
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItemActionListener


class ResultItemActionDelegate(
    private val viewState: ResultViewState,
    private val router: ResultRouter,
    private val curtainDelegate: ResultCurtainMenuDelegate,
    private val interactor: ResultInteractor,
    private val preferenceStore: PreferenceStore,
) : ResultItemActionListener {
    override fun onItemClick(item: Node) {
        when {
            item.content is NodeContent.File.Text -> router.openFile(item.path, viewState.task.value.uuid)
            item.isDirectory -> Unit // todo open dir
            else -> router.openWith(item)
        }
    }

    override fun onItemLongClick(item: Node) = viewState.run {
        val matches = (task.value.result as SearchResult.FinderResult).matches
        val checked = checked.value.filter { id ->
            matches.any { it.item.uniqueId == id && !it.isDeleting }
        }
        val isSingle = !checked.contains(item.uniqueId) || checked.size == 1
        val options = when {
            isSingle && item.isDirectory -> ExplorerItemOptions(oneDirOptions, listOf(item), composition.value)
            isSingle -> ExplorerItemOptions(oneFileOptions, listOf(item), composition.value)
            else -> {
                val items = matches.filter { checked.contains(it.item.uniqueId) }.map { it.item }
                ExplorerItemOptions(manyFilesOptions, items, composition.value)
            }
        }
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