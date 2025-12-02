package app.atomofiron.searchboxapp.di.dependencies.channel

import app.atomofiron.searchboxapp.model.other.AppState
import kotlinx.coroutines.flow.MutableStateFlow

class CommonChannel {
    val appState = MutableStateFlow<AppState>(AppState.Unknown)
}