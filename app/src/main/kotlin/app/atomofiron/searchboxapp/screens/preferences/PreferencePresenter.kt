package app.atomofiron.searchboxapp.screens.preferences

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.preference.UpdateActionListener
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.ExportImportDelegate
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceClickOutput
import app.atomofiron.searchboxapp.utils.Shell
import kotlinx.coroutines.CoroutineScope

class PreferencePresenter(
    scope: CoroutineScope,
    private val viewState: PreferenceViewState,
    router: PreferenceRouter,
    exportImportDelegate: ExportImportDelegate.ExportImportOutput,
    preferenceClickOutput: PreferenceClickOutput,
    private val preferenceStore: PreferenceStore,
    appStore: AppStore,
    updateDelegate: UpdateActionListener,
) : BasePresenter<PreferenceViewModel, PreferenceRouter>(scope, router),
    ExportImportDelegate.ExportImportOutput by exportImportDelegate,
    PreferenceClickOutput by preferenceClickOutput,
    UpdateActionListener by updateDelegate
{

    val resources by appStore.resourcesProperty

    init {
        preferenceStore.appTheme.collect(scope) {
            viewState.showDeepBlack.value = it !is AppTheme.Light
        }
        onSubscribeData()
    }

    override fun onSubscribeData() {
        preferenceStore.useSu.collect(scope) {
            if (it) onUseSuEnabled()
        }
    }

    private suspend fun onUseSuEnabled() {
        val output = Shell.checkSu()
        if (!output.success) {
            val error = output.error.trim()
            preferenceStore.setUseSu(false)
            val message = error
                .takeIf { it.isNotBlank() }
                ?: resources.getString(R.string.not_allowed)
            viewState.showAlert(message)
        }
    }
}