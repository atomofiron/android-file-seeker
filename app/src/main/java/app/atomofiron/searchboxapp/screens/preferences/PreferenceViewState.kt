package app.atomofiron.searchboxapp.screens.preferences

import android.annotation.SuppressLint
import android.content.Context
import androidx.preference.PreferenceDataStore
import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.utils.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class PreferenceViewState(private val scope: CoroutineScope) {

    @Inject
    lateinit var preferenceDataStore: PreferenceDataStore
    @SuppressLint("StaticFieldLeak")
    @Inject
    lateinit var appContext: Context

    val alert = ChannelFlow<String>()
    val alertOutputSuccess = ChannelFlow<Int>()
    val alertOutputError = ChannelFlow<Shell.Output>()
    val showDeepBlack = MutableStateFlow(false)
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