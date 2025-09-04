package app.atomofiron.searchboxapp.screens.main

import app.atomofiron.searchboxapp.di.dependencies.delegate.InitialDelegate
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.main.util.tasks.XTask
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewState(
    val activityMode: ActivityMode,
    val preferences: PreferenceStore,
    initialDelegate: InitialDelegate,
) {

    val setOrientation = preferences.appOrientation
    val setJoystick = preferences.joystickComposition
    val hapticFeedback = preferences.hapticFeedback
    val tasks = MutableStateFlow<List<XTask>>(listOf())
    val setTheme = MutableStateFlow(initialDelegate.getTheme())
}