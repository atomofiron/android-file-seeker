package app.atomofiron.searchboxapp.di.dependencies.store

import app.atomofiron.searchboxapp.model.other.AppUpdateState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppUpdateStore @Inject constructor() {

    private var fallback: AppUpdateState = AppUpdateState.Unknown
    val state: StateFlow<AppUpdateState>
        field = MutableStateFlow<AppUpdateState>(AppUpdateState.Unknown)

    fun set(state: AppUpdateState) {
        when (state) {
            is AppUpdateState.Completable,
            is AppUpdateState.Available -> fallback = state
            else -> Unit
        }
        this.state.value = state
    }

    fun fallback() {
        state.value = fallback
    }
}