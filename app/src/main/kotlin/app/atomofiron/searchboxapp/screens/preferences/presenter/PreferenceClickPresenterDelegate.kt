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
import app.atomofiron.searchboxapp.di.dependencies.store.Strings
import app.atomofiron.searchboxapp.model.other.toUni
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainKey
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

private data object AboutCurtainKey : CurtainKey()
private data object ExportImportCurtainKey : CurtainKey()
private data object ExplorerItemCurtainKey : CurtainKey()
private data object JoystickCurtainKey : CurtainKey()
private data object ColorSchemeCurtainKey : CurtainKey()

@PreferenceScope
class PreferenceClickPresenterDelegate @Inject constructor(
    private val context: Context,
    scope: CoroutineScope,
    private val viewState: PreferenceViewState,
    private val router: PreferenceRouter,
    curtainChannel: CurtainChannel,
    resources: AppResources,
    private val dialogs: DialogDelegate,
    private val aboutDelegate: Lazy<AboutDelegate>,
    private val exportImportDelegate: Lazy<ExportImportDelegate>,
    private val explorerItemDelegate: Lazy<ExplorerItemDelegate>,
    private val joystickDelegate: Lazy<JoystickDelegate>,
    private val colorSchemeDelegate: Lazy<ColorSchemeDelegate>,
) : Recipient, PreferenceClickOutput, Strings by resources {

    init {
        curtainChannel.flow.collectForMe(scope) { controller ->
            controller ?: return@collectForMe
            val adapter: CurtainApi.Adapter<*> = when (controller.requestId) {
                AboutCurtainKey.id -> aboutDelegate.get()
                ExportImportCurtainKey.id -> exportImportDelegate.get()
                ExplorerItemCurtainKey.id -> explorerItemDelegate.get()
                JoystickCurtainKey.id -> joystickDelegate.get()
                ColorSchemeCurtainKey.id -> colorSchemeDelegate.get()
                else -> return@collectForMe
            }
            adapter.setController(controller)
        }
    }

    override fun onAboutClick() = router.showCurtain(AboutCurtainKey, recipient)

    override fun onColorSchemeClick() = router.showCurtain(ColorSchemeCurtainKey, recipient)

    override fun onExportImportClick() = router.showCurtain(ExportImportCurtainKey, recipient)

    override fun onExplorerItemClick() = router.showCurtain(ExplorerItemCurtainKey, recipient)

    override fun onJoystickClick() = router.showCurtain(JoystickCurtainKey, recipient)

    override fun onLocaleClick() {
        if (Android.T) router.showLocaleSettings()
    }

    override fun onUseSuChanged(value: Boolean): Boolean {
        val result = context.verifyNativeBin()
        if (result is Rslt.Err) {
            val message = result.message
                .takeIf { it.isNotBlank() }
                ?: get(R.string.not_allowed)
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