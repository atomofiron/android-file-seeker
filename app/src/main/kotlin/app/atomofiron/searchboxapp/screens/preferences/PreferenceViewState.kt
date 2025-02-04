package app.atomofiron.searchboxapp.screens.preferences

import androidx.preference.PreferenceDataStore
import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.store.AppUpdateStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.other.AppUpdateState
import app.atomofiron.searchboxapp.model.preference.ToyboxVariant
import app.atomofiron.searchboxapp.utils.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import debug.LeakWatcher
import kotlinx.coroutines.flow.merge

class PreferenceViewState(
    private val scope: CoroutineScope,
    val preferenceDataStore: PreferenceDataStore,
    preferenceStore: PreferenceStore,
    preferenceChannel: PreferenceChannel,
    updateStore: AppUpdateStore,
    appWatcher: LeakWatcher,
) {
    private val _alerts = ChannelFlow<String>()
    val alerts = merge(preferenceChannel.appUpdateStatus, _alerts)
    val alertOutputSuccess = ChannelFlow<Int>()
    val alertOutputError = ChannelFlow<Shell.Output>()
    val showDeepBlack = MutableStateFlow(false)
    val useSu: StateFlow<Boolean> = preferenceStore.useSu
    val toybox: StateFlow<ToyboxVariant> = preferenceStore.toyboxVariant
    val withDebugGroup = appWatcher.isAvailable
    val appUpdate: StateFlow<AppUpdateState> = updateStore.state
    // todo zip and share the backup
    val isExportImportAvailable: Boolean = true

    fun showAlert(value: String) {
        _alerts[scope] = value
    }

    fun sendAlertOutputSuccess(value: Int) {
        alertOutputSuccess[scope] = value
    }

    fun sendAlertOutputError(value: Shell.Output) {
        alertOutputError[scope] = value
    }
}