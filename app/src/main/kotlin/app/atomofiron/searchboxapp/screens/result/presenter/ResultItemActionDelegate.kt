package app.atomofiron.searchboxapp.screens.result.presenter

import app.atomofiron.common.util.AlertMessage
import app.atomofiron.common.util.dialog.DialogDelegate
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.dependencies.interactor.ResultInteractor
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.other.toUni
import app.atomofiron.searchboxapp.screens.common.delegates.FileOperationsDelegate
import app.atomofiron.searchboxapp.screens.result.ResultRouter
import app.atomofiron.searchboxapp.screens.result.ResultViewState
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItem
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItemActionListener
import app.atomofiron.searchboxapp.utils.Rslt

class ResultItemActionDelegate(
    private val viewState: ResultViewState,
    private val operations: FileOperationsDelegate,
    private val router: ResultRouter,
    private val curtainDelegate: ResultCurtainMenuDelegate,
    private val dialogs: DialogDelegate,
    private val interactor: ResultInteractor,
) : ResultItemActionListener {
    override fun onItemClick(item: Node) {
        when {
            item.isDirectory -> Unit // todo open dir
            item.content is NodeContent.Text -> router.openFile(item.ref, viewState.taskUuid)
            item.content is NodeContent.AndroidApp -> operations.askForAndroidApp(item.content)
            else -> router.openWith(item)
        }
    }

    override fun onItemLongClick(item: Node) = viewState.run {
        val matches = result.value.matches
        val items = when {
            item.isChecked -> matches.mapNotNull { it.item.takeIf { checked.value.contains(it.uniqueId) } }
            else -> listOf(item)
        }
        val options = operations.operations(items, readOnly = true)
        when (options) {
            is Rslt.Ok -> curtainDelegate.showOptions(options.value)
            is Rslt.Err -> when {
                options.isEmpty -> AlertMessage(R.string.unknown_error)
                else -> AlertMessage(options.message)
            }.let { viewState.showAlert(it) }
        }
    }

    override fun onItemCheck(item: Node, toChecked: Boolean): Boolean {
        val checked = viewState.checked.value.toMutableList()
        when {
            toChecked -> checked.add(item.uniqueId)
            else -> checked.remove(item.uniqueId)
        }
        viewState.checked.value = checked
        return true
    }

    override fun onItemVisible(item: ResultItem.Item) = interactor.update(viewState.taskUuid, item.match)

    override fun onErrorsClick() {
        val error = viewState.result.value.errors.joinToString(separator = "\n")
        dialogs.showErrors(error.toUni())
    }
}