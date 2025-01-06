package app.atomofiron.searchboxapp.screens.result

import app.atomofiron.common.util.flow.*
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.model.finder.SearchTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class ResultViewState(
    private val scope: CoroutineScope,
) {

    val oneFileOptions = listOf(R.id.menu_remove, R.id.menu_share, R.id.menu_open_with)
    val oneDirOptions = listOf(R.id.menu_remove)
    val manyFilesOptions = listOf(R.id.menu_remove)

    val task = DeferredStateFlow<SearchTask>()
    val composition = DeferredStateFlow<ExplorerItemComposition>()
    val alerts = ChannelFlow<String>()
    val checked = MutableStateFlow(listOf<Int>())

    fun sendAlert(value: String) {
        alerts[scope] = value
    }
}