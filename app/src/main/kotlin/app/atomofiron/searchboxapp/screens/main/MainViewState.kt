package app.atomofiron.searchboxapp.screens.main

import app.atomofiron.searchboxapp.di.dependencies.delegate.InitialDelegate
import app.atomofiron.searchboxapp.di.dependencies.store.EasterEggStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.main.model.EasterEgg
import app.atomofiron.searchboxapp.screens.main.util.tasks.XTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@MainScope
class MainViewState @Inject constructor(
    val activityMode: ActivityMode,
    preferences: PreferenceStore,
    eggStore: EasterEggStore,
    initialDelegate: InitialDelegate,
) {
    val setOrientation = preferences.appOrientation
    val setJoystick = preferences.joystickComposition
    val hapticFeedback = preferences.hapticFeedback
    val tasks = MutableStateFlow<List<XTask>>(listOf())
    val setTheme = MutableStateFlow(initialDelegate.getTheme())
    val easterEgg: Flow<EasterEgg?> = combine(eggStore.value, preferences.eggHalloween, preferences.eggNewYear, preferences.eggClown, ::easterEgg)

    private fun easterEgg(egg: EasterEgg?, halloween: Boolean, newYear: Boolean, clown: Boolean): EasterEgg? {
        return when (egg) {
            null -> null
            EasterEgg.Halloween -> egg.takeIf { halloween }
            EasterEgg.NewYear -> egg.takeIf { newYear }
            EasterEgg.Clown -> egg.takeIf { clown }
        }
    }
}