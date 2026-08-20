package app.atomofiron.searchboxapp.di.dependencies.store

import app.atomofiron.searchboxapp.screens.main.model.EasterEgg
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EasterEggStore @Inject constructor() {

    val value: StateFlow<EasterEgg?>
        field = MutableStateFlow<EasterEgg?>(null)

    fun set(value: EasterEgg?) {
        this.value.value = value
    }
}