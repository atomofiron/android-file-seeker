package app.atomofiron.searchboxapp.screens.preferences

import androidx.preference.PreferenceDataStore
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
    val showDeepBlack = MutableStateFlow(false)
    val asSu: StateFlow<Boolean> = preferenceStore.asSu
    val hapticFeedback: StateFlow<Boolean> = preferenceStore.hapticFeedback
    val screenshotOpsError: StateFlow<String?> = ScreenshotService.error
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
}