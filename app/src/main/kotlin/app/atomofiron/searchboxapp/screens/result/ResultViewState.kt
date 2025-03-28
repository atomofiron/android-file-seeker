package app.atomofiron.searchboxapp.screens.result

import app.atomofiron.common.util.AlertMessage
import app.atomofiron.common.util.flow.*
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.model.finder.SearchTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class ResultViewState(
    private val scope: CoroutineScope,
) {

    val oneFileOptions = listOf(R.id.menu_delete, R.id.menu_share, R.id.menu_open_with, R.id.menu_copy_path)
    val oneDirOptions = listOf(R.id.menu_delete, R.id.menu_copy_path)
    val manyFilesOptions = listOf(R.id.menu_delete)

    val task = DeferredStateFlow<SearchTask>()
    val composition = DeferredStateFlow<ExplorerItemComposition>()
    val alerts = ChannelFlow<AlertMessage.Res>()
    val checked = MutableStateFlow(listOf<Int>())

    fun showAlert(message: AlertMessage.Res) {
        alerts[scope] = message
    }
}