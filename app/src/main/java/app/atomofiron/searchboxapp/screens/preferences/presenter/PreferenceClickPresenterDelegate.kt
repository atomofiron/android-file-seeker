package app.atomofiron.searchboxapp.screens.preferences.presenter

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.Recipient
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.injectable.channel.CurtainChannel
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.preferences.PreferenceRouter
import app.atomofiron.searchboxapp.screens.preferences.PreferenceViewModel
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceClickOutput
import app.atomofiron.searchboxapp.screens.preferences.presenter.curtain.*
import app.atomofiron.searchboxapp.utils.showCurtain

class PreferenceClickPresenterDelegate(
    viewModel: PreferenceViewModel,
    private val router: PreferenceRouter,
    private val exportImportDelegate: ExportImportDelegate.ExportImportOutput,
    private val preferenceStore: PreferenceStore,
    curtainChannel: CurtainChannel,
) : Recipient, PreferenceClickOutput {

    init {
        curtainChannel.flow.collectForMe(viewModel.viewModelScope) { controller ->
            controller ?: return@collectForMe
            val adapter: CurtainApi.Adapter<*> = when (controller.requestId) {
                R.layout.curtain_about -> AboutDelegate()
                R.layout.curtain_preference_export_import -> ExportImportDelegate(exportImportDelegate)
                R.layout.curtain_preference_explorer_item -> ExplorerItemDelegate(preferenceStore)
                R.layout.curtain_preference_joystick -> JoystickDelegate(preferenceStore)
                R.layout.curtain_preference_toybox -> ToyboxDelegate(preferenceStore)
                else -> return@collectForMe
            }
            adapter.setController(controller)
        }
    }

    override fun onAboutClick() = router.showCurtain(recipient, R.layout.curtain_about)

    override fun onExportImportClick() = router.showCurtain(recipient, R.layout.curtain_preference_export_import)

    override fun onExplorerItemClick() = router.showCurtain(recipient, R.layout.curtain_preference_explorer_item)

    override fun onJoystickClick() = router.showCurtain(recipient, R.layout.curtain_preference_joystick)

    override fun onToyboxClick() = router.showCurtain(recipient, R.layout.curtain_preference_toybox)
}