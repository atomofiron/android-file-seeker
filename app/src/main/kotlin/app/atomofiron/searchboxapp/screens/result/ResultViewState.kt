package app.atomofiron.searchboxapp.screens.result

import app.atomofiron.common.util.Alert
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItem
import app.atomofiron.searchboxapp.screens.result.state.ResultDockState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ResultViewState {
    val mode: ActivityMode
    val isReady: StateFlow<Boolean>
    val items: StateFlow<List<ResultItem>>
    val updates: Flow<ResultItem>
    val dock: StateFlow<ResultDockState>
    val composition: Flow<ExplorerItemComposition>
    val alerts: Flow<Alert>
}