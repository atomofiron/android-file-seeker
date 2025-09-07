package app.atomofiron.searchboxapp.screens.preferences.presenter

import app.atomofiron.common.arch.Recipient
import app.atomofiron.common.util.Android
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.di.dependencies.store.AppResources
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.AppSource
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.preferences.PreferenceRouter
import app.atomofiron.searchboxapp.screens.preferences.PreferenceViewState
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceClickOutput
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.*
import app.atomofiron.searchboxapp.utils.Shell
import kotlinx.coroutines.CoroutineScope

class PreferenceClickPresenterDelegate(
    scope: CoroutineScope,
    private val viewState: PreferenceViewState,
    private val router: PreferenceRouter,
    private val exportImportDelegate: ExportImportDelegate.ExportImportOutput,
    private val preferenceStore: PreferenceStore,
    curtainChannel: CurtainChannel,
    resources: AppResources,
    appSource: AppSource,
) : Recipient, PreferenceClickOutput {

    val resources by resources

    init {
        curtainChannel.flow.collectForMe(scope) { controller ->
            controller ?: return@collectForMe
            val adapter: CurtainApi.Adapter<*> = when (controller.requestId) {
                R.layout.curtain_about -> AboutDelegate(router, appSource)
                R.layout.curtain_preference_export_import -> ExportImportDelegate(exportImportDelegate)
                R.layout.curtain_preference_explorer_item -> ExplorerItemDelegate(preferenceStore, resources)
                R.layout.curtain_preference_joystick -> JoystickDelegate(preferenceStore)
                R.layout.curtain_color_scheme -> ColorSchemeDelegate()
                else -> return@collectForMe
            }
            adapter.setController(controller)
        }
    }

    override fun onAboutClick() = router.showCurtain(recipient, R.layout.curtain_about)

    override fun onColorSchemeClick() = router.showCurtain(recipient, R.layout.curtain_color_scheme)

    override fun onExportImportClick() = router.showCurtain(recipient, R.layout.curtain_preference_export_import)

    override fun onExplorerItemClick() = router.showCurtain(recipient, R.layout.curtain_preference_explorer_item)

    override fun onJoystickClick() = router.showCurtain(recipient, R.layout.curtain_preference_joystick)

    override fun onLocaleClick() {
        if (Android.T) router.showLocaleSettings()
    }

    override fun onUseSuChanged(value: Boolean): Boolean {
        val output = Shell.checkSu()
        if (!output.success) {
            val message = output.error.trim()
                .takeIf { it.isNotBlank() }
                ?: resources.getString(R.string.not_allowed)
            viewState.showAlert(message)
        }
        return output.success
    }
}