package app.atomofiron.searchboxapp.screens.preferences

import androidx.preference.PreferenceDataStore
import app.atomofiron.common.util.Alert
import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.android.ScreenshotService
import app.atomofiron.searchboxapp.di.dependencies.channel.PreferenceChannel
import app.atomofiron.searchboxapp.di.dependencies.store.AppUpdateStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.other.AppUpdateState
import debug.LeakWatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@PreferenceScope
class PreferenceViewState @Inject constructor(
    private val scope: CoroutineScope,
    val preferenceDataStore: PreferenceDataStore,
    preferenceStore: PreferenceStore,
    preferenceChannel: PreferenceChannel,
    updateStore: AppUpdateStore,
    appWatcher: LeakWatcher,
) {
    private val _alerts = ChannelFlow<Alert>()
    val alerts = merge(preferenceChannel.appUpdateStatus.map(Alert::invoke), _alerts)
    val showDeepBlack = MutableStateFlow(false)
    val asSu: StateFlow<Boolean> = preferenceStore.asSu
    val hapticFeedback: StateFlow<Boolean> = preferenceStore.hapticFeedback
    val screenshotOpsError: StateFlow<String?> = ScreenshotService.error
    val withDebugGroup = appWatcher.isAvailable
    val appUpdate: StateFlow<AppUpdateState> = updateStore.state
    // todo zip and share the backup
    val isExportImportAvailable: Boolean = true

    fun showAlert(alert: Alert) {
        _alerts[scope] = alert
    }
}