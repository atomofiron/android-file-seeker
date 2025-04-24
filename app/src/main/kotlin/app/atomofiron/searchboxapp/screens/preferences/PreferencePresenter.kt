package app.atomofiron.searchboxapp.screens.preferences

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.custom.preference.UpdateActionListener
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.ExportImportDelegate
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceClickOutput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PreferencePresenter(
    scope: CoroutineScope,
    private val viewState: PreferenceViewState,
    router: PreferenceRouter,
    exportImportDelegate: ExportImportDelegate.ExportImportOutput,
    preferenceClickOutput: PreferenceClickOutput,
    preferenceStore: PreferenceStore,
    updateDelegate: UpdateActionListener,
) : BasePresenter<PreferenceViewModel, PreferenceRouter>(scope, router),
    ExportImportDelegate.ExportImportOutput by exportImportDelegate,
    PreferenceClickOutput by preferenceClickOutput,
    UpdateActionListener by updateDelegate
{

    init {
        preferenceStore.appTheme.collect(scope) {
            viewState.showDeepBlack.value = it !is AppTheme.Light
        }
        scope.launch {
            val useSu = preferenceStore.useSu.value
            if (useSu && !onUseSuChanged(true)) {
                preferenceStore { setUseSu(false) }
            }
        }
    }

    override fun onSubscribeData() = Unit
}