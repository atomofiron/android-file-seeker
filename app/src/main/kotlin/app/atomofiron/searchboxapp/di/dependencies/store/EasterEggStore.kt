package app.atomofiron.searchboxapp.di.dependencies.store

import app.atomofiron.searchboxapp.screens.main.model.EasterEgg
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EasterEggStore @Inject constructor() {

    private val _value = MutableStateFlow<EasterEgg?>(null)
    val value: StateFlow<EasterEgg?> = _value

    fun set(value: EasterEgg?) {
        _value.value = value
    }
}