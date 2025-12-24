package app.atomofiron.searchboxapp.screens.preferences.presenter

import app.atomofiron.common.util.Alert
import app.atomofiron.common.util.dialog.DialogDelegate
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.dependencies.channel.PreferenceChannel
import app.atomofiron.searchboxapp.di.dependencies.service.PreferenceService
import app.atomofiron.searchboxapp.model.other.toUni
import app.atomofiron.searchboxapp.screens.preferences.PreferenceViewState
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.ExportImportDelegate
import app.atomofiron.searchboxapp.utils.Rslt
import kotlinx.coroutines.CoroutineScope

class ExportImportPresenterDelegate(
    private val scope: CoroutineScope,
    private val viewState: PreferenceViewState,
    private val preferenceService: PreferenceService,
    private val preferenceChannel: PreferenceChannel,
    private val dialogs: DialogDelegate,
) : ExportImportDelegate.ExportImportOutput {

    override fun exportPreferences() {
        val result = preferenceService.exportPreferences()
        show(result, Alert(R.string.successful))
    }

    override fun exportHistory() {
        val result = preferenceService.exportHistory()
        show(result, Alert(R.string.successful))
    }

    override fun importPreferences() {
        val result = preferenceService.importPreferences()
        show(result, Alert(R.string.successful_with_restart, important = true))
    }

    override fun importHistory() {
        val result = preferenceService.importHistory()
        show(result, Alert(R.string.successful))
        if (result.isOk) {
            preferenceChannel.onHistoryImported.invoke(scope)
        }
    }

    private fun show(result: Rslt<Unit>, alert: Alert) {
        when (result) {
            is Rslt.Ok -> viewState.showAlert(alert)
            is Rslt.Err -> dialogs.showError(result.message.toUni())
        }
    }
}