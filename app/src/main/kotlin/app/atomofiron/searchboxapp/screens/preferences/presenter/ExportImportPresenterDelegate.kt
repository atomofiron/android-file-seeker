package app.atomofiron.searchboxapp.screens.preferences.presenter

import app.atomofiron.common.util.flow.invoke
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.dependencies.channel.PreferenceChannel
import app.atomofiron.searchboxapp.di.dependencies.service.PreferenceService
import app.atomofiron.searchboxapp.screens.preferences.PreferenceViewState
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.ExportImportDelegate
import app.atomofiron.searchboxapp.utils.Shell
import kotlinx.coroutines.CoroutineScope

class ExportImportPresenterDelegate(
    private val scope: CoroutineScope,
    private val viewState: PreferenceViewState,
    private val preferenceService: PreferenceService,
    private val preferenceChannel: PreferenceChannel,
) : ExportImportDelegate.ExportImportOutput {

    override fun exportPreferences() {
        val output = preferenceService.exportPreferences()
        showOutput(output, R.string.successful)
    }

    override fun exportHistory() {
        val output = preferenceService.exportHistory()
        showOutput(output, R.string.successful)
    }

    override fun importPreferences() {
        val output = preferenceService.importPreferences()
        showOutput(output, R.string.successful_with_restart)
    }

    override fun importHistory() {
        val output = preferenceService.importHistory()
        showOutput(output, R.string.successful)
        if (output.success) {
            preferenceChannel.onHistoryImported.invoke(scope)
        }
    }

    private fun showOutput(output: Shell.Output, successMessage: Int) {
        when {
            output.success -> viewState.sendAlertOutputSuccess(successMessage)
            else -> viewState.sendAlertOutputError(output)
        }
    }
}