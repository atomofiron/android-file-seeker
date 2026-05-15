package app.atomofiron.searchboxapp.screens.result.presenter

import app.atomofiron.common.util.AlertErr
import app.atomofiron.common.util.dialog.DialogDelegate
import app.atomofiron.common.util.extension.launchOnIO
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.dependencies.router.FileSharingDelegate
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.other.toUni
import app.atomofiron.searchboxapp.screens.common.delegates.ApkOperationsDelegate
import app.atomofiron.searchboxapp.screens.common.delegates.FileOperationDelegate
import app.atomofiron.searchboxapp.screens.result.ResultRouter
import app.atomofiron.searchboxapp.screens.result.ResultScope
import app.atomofiron.searchboxapp.screens.result.ResultViewState
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItem
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItemActionListener
import app.atomofiron.searchboxapp.screens.result.di.ResultInteractor
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.toAlert
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@ResultScope
class ResultItemActionDelegate @Inject constructor(
    private val viewState: ResultViewState,
    private val scope: CoroutineScope,
    private val operations: FileOperationDelegate,
    private val apks: ApkOperationsDelegate,
    private val router: ResultRouter,
    private val curtainDelegate: ResultCurtainMenuDelegate,
    private val dialogs: DialogDelegate,
    private val interactor: ResultInteractor,
    private val sharing: FileSharingDelegate,
) : ResultItemActionListener {

    override fun onItemClick(item: Node) {
        when (true) {
            item.isDirectory -> Unit // todo open dir
            (item.error is NodeError.NoSuchFileOrDir),
            (item.error is NodeError.PermissionDenied) -> viewState.showAlert(item.error.toAlert(item.content))
            (item.content is NodeContent.Text) -> router.openFile(item.ref, viewState.taskUuid)
            (item.content is NodeContent.AndroidApp) -> apks.askForAndroidApp(item.ref, item.content)
            else -> sharing.openWith(item)
        }
    }

    override fun onItemLongClick(item: Node) = viewState.run {
        val items = when {
            item.isChecked -> cache.values.mapNotNull { item ->
                item.item.takeIf { checked.value.contains(item.uniqueId) }
            }
            else -> listOf(item)
        }
        val options = operations.operations(items, readOnly = true)
        when (options) {
            is Rslt.Ok -> curtainDelegate.showOptions(options.value)
            is Rslt.Err -> when {
                options.isEmpty -> AlertErr(R.string.unknown_error)
                else -> AlertErr(options.message)
            }.let { viewState.showAlert(it) }
        }
    }

    override fun onItemCheck(item: Node, toChecked: Boolean): Boolean {
        viewState.setChecked(item.uniqueId, toChecked)
        return true
    }

    override fun onItemVisible(item: ResultItem.Item) {
        scope.launchOnIO {
            val updated = interactor.update(item.item)
            val meta = interactor.usage(updated)
            updated.copy(meta = meta)
                .takeIf { it != item.item }
                ?.copy(isChecked = false)
                ?.let { item.copy(item = it) }
                ?.let { viewState.cache(it) }
        }
    }

    override fun onErrorsClick() {
        val error = viewState.result.errors.joinToString(separator = "\n")
        dialogs.showErrors(error.toUni())
    }
}