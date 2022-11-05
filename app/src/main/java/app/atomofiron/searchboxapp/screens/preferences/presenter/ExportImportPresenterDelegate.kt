package app.atomofiron.searchboxapp.screens.preferences.presenter

import android.content.Context
import androidx.lifecycle.viewModelScope
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.service.PreferenceService
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.screens.preferences.PreferenceViewModel
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.ExportImportDelegate
import app.atomofiron.searchboxapp.utils.Explorer
import app.atomofiron.searchboxapp.utils.Shell

class ExportImportPresenterDelegate(
    context: Context,
    private val viewModel: PreferenceViewModel,
    private val preferenceService: PreferenceService,
    private val preferenceStore: PreferenceStore,
    private val preferenceChannel: PreferenceChannel,
) : ExportImportDelegate.ExportImportOutput {
    override val externalPath = Explorer.completeDirPath(context.getExternalFilesDir(null)!!.absolutePath)

    init {
        preferenceStore.appTheme.collect(viewModel.viewModelScope) {
            viewModel.showDeepBlack.value = it !is AppTheme.Light
        }
    }

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
            preferenceChannel.historyImportedEvent.invoke()
        }
    }

    private fun showOutput(output: Shell.Output, successMessage: Int) {
        when {
            output.success -> viewModel.alertOutputSuccess.value = successMessage
            else -> viewModel.alertOutputError.value = output
        }
    }
}