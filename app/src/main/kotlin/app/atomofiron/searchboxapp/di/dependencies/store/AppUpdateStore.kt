package app.atomofiron.searchboxapp.di.dependencies.store

import app.atomofiron.searchboxapp.model.other.AppUpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppUpdateStore {
    private var fallback: AppUpdateState = AppUpdateState.Unknown
    private val _state = MutableStateFlow<AppUpdateState>(AppUpdateState.Unknown)
    val state: StateFlow<AppUpdateState> = _state

    fun set(state: AppUpdateState) {
        when (state) {
            is AppUpdateState.Completable,
            is AppUpdateState.Available -> fallback = state
            else -> Unit
        }
        _state.value = state
    }

    fun fallback() {
        _state.value = fallback
    }
}