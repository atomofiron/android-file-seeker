package app.atomofiron.searchboxapp.screens.preferences

import androidx.preference.PreferenceDataStore
import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.injectable.store.AppUpdateStore
import app.atomofiron.searchboxapp.model.other.AppUpdateState
import app.atomofiron.searchboxapp.utils.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferenceViewState(
    private val scope: CoroutineScope,
    val preferenceDataStore: PreferenceDataStore,
    updateStore: AppUpdateStore,
) {
    val alert = ChannelFlow<String>()
    val alertOutputSuccess = ChannelFlow<Int>()
    val alertOutputError = ChannelFlow<Shell.Output>()
    val showDeepBlack = MutableStateFlow(false)
    val appUpdate: StateFlow<AppUpdateState> = updateStore.state
    // todo zip and share the backup
    val isExportImportAvailable: Boolean = true

    fun showAlert(value: String) {
        alert[scope] = value
    }

    fun sendAlertOutputSuccess(value: Int) {
        alertOutputSuccess[scope] = value
    }

    fun sendAlertOutputError(value: Shell.Output) {
        alertOutputError[scope] = value
    }
}