package app.atomofiron.searchboxapp.screens.preferences.presenter

import android.content.Context
import app.atomofiron.common.arch.Recipient
import app.atomofiron.common.util.Alert
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.dialog.DialogDelegate
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.android.verifyNativeBin
import app.atomofiron.searchboxapp.di.dependencies.channel.CurtainChannel
import app.atomofiron.searchboxapp.di.dependencies.store.AppResources
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.other.toUni
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.preferences.PreferenceRouter
import app.atomofiron.searchboxapp.screens.preferences.PreferenceScope
import app.atomofiron.searchboxapp.screens.preferences.PreferenceViewState
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceClickOutput
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.AboutDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.ColorSchemeDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.ExplorerItemDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.ExportImportDelegate
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.JoystickDelegate
import app.atomofiron.searchboxapp.utils.Const.LF
import app.atomofiron.searchboxapp.utils.Rslt
import dagger.Lazy
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@PreferenceScope
class PreferenceClickPresenterDelegate @Inject constructor(
    private val context: Context,
    scope: CoroutineScope,
    private val viewState: PreferenceViewState,
    private val router: PreferenceRouter,
    private val exportImportDelegate: ExportImportDelegate.ExportImportOutput,
    private val preferenceStore: PreferenceStore,
    curtainChannel: CurtainChannel,
    resources: AppResources,
    private val dialogs: DialogDelegate,
    private val aboutDelegate: Lazy<AboutDelegate>,
) : Recipient, PreferenceClickOutput {

    val resources by resources

    init {
        curtainChannel.flow.collectForMe(scope) { controller ->
            controller ?: return@collectForMe
            val adapter: CurtainApi.Adapter<*> = when (controller.requestId) {
                R.layout.curtain_about -> aboutDelegate.get()
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
        val result = context.verifyNativeBin()
        if (result is Rslt.Err) {
            val message = result.message
                .takeIf { it.isNotBlank() }
                ?: resources.getString(R.string.not_allowed)
            val first = message.indexOfFirst { it == LF }
            val last = message.indexOfLast { it == LF }
            when {
                first != last -> dialogs.showError(message.toUni())
                else -> viewState.showAlert(Alert(message))
            }
        }
        return result.isOk
    }
}