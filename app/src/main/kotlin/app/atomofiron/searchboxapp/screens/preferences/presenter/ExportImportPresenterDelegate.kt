package app.atomofiron.searchboxapp.screens.preferences.presenter

import app.atomofiron.common.util.flow.invoke
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.dependencies.channel.PreferenceChannel
import app.atomofiron.searchboxapp.di.dependencies.service.PreferenceService
import app.atomofiron.searchboxapp.screens.preferences.PreferenceViewState
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.ExportImportDelegate
import app.atomofiron.searchboxapp.utils.Rslt
import kotlinx.coroutines.CoroutineScope

class ExportImportPresenterDelegate(
    private val scope: CoroutineScope,
    private val viewState: PreferenceViewState,
    private val preferenceService: PreferenceService,
    private val preferenceChannel: PreferenceChannel,
) : ExportImportDelegate.ExportImportOutput {

    override fun exportPreferences() {
        val result = preferenceService.exportPreferences()
        showOutput(result, R.string.successful)
    }

    override fun exportHistory() {
        val result = preferenceService.exportHistory()
        showOutput(result, R.string.successful)
    }

    override fun importPreferences() {
        val result = preferenceService.importPreferences()
        showOutput(result, R.string.successful_with_restart)
    }

    override fun importHistory() {
        val result = preferenceService.importHistory()
        showOutput(result, R.string.successful)
        if (result.isOk) {
            preferenceChannel.onHistoryImported.invoke(scope)
        }
    }

    private fun showOutput(result: Rslt<Unit>, successMessage: Int) {
        when (result) {
            is Rslt.Ok -> viewState.sendAlertOutputSuccess(successMessage)
            is Rslt.Err -> viewState.sendAlertOutputError(result)
        }
    }
}