package app.atomofiron.searchboxapp.di.dependencies.channel

import app.atomofiron.searchboxapp.model.other.AppScreen
import app.atomofiron.searchboxapp.model.other.AppState
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommonChannel @Inject constructor() {
    val appState = MutableStateFlow<AppState>(AppState.Unknown)
    val currentScreen = MutableStateFlow<AppScreen>(AppScreen.Unknown)
}