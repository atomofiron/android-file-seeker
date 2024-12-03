package app.atomofiron.searchboxapp.screens.main

import app.atomofiron.searchboxapp.injectable.delegate.InitialDelegate
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.main.util.tasks.XTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewState(
    private val scope: CoroutineScope,
    val preferenceStore: PreferenceStore,
    initialDelegate: InitialDelegate,
) {

    val setOrientation = preferenceStore.appOrientation
    val setJoystick = preferenceStore.joystickComposition
    val tasks = MutableStateFlow<List<XTask>>(listOf())
    val setTheme = MutableStateFlow(initialDelegate.getTheme())
}