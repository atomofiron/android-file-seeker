package app.atomofiron.searchboxapp.screens.result.presenter

import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.injectable.interactor.ResultInteractor
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.other.ExplorerItemOptions
import app.atomofiron.searchboxapp.screens.result.ResultRouter
import app.atomofiron.searchboxapp.screens.result.ResultViewModel
import app.atomofiron.searchboxapp.screens.result.adapter.FinderResultItem
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItemActionListener
import app.atomofiron.searchboxapp.utils.Util

class ResultItemActionDelegate(
    private val viewModel: ResultViewModel,
    private val router: ResultRouter,
    private val curtainM: ResultCurtainMenuDelegate,
    private val interactor: ResultInteractor,
    private val preferenceStore: PreferenceStore,
) : ResultItemActionListener {
    override fun onItemClick(item: Node) {
        val textFormats = preferenceStore.textFormats.entity
        if (item.isFile && Util.isTextFile(item.path, textFormats)) {
            val params = viewModel.task.value.params
            router.openFile(item.path, params)
        } else {
            // todo open dir
        }
    }

    override fun onItemLongClick(item: Node) = viewModel.run {
        val options = when {
            checked.contains(item) -> ExplorerItemOptions(manyFilesOptions, checked, composition.value)
            else -> ExplorerItemOptions(oneFileOptions, listOf(item), composition.value)
        }
        curtainM.showOptions(options)
    }

    override fun onItemCheck(item: Node, isChecked: Boolean) {
        // todo item.isChecked = isChecked
        when {
            isChecked -> viewModel.checked.add(item)
            else -> viewModel.checked.remove(item)
        }
        viewModel.enableOptions.value = viewModel.checked.isNotEmpty()
    }

    override fun onItemVisible(item: FinderResultItem.Item) = interactor.cacheFile(item)
}