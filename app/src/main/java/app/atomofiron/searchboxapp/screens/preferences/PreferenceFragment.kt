package app.atomofiron.searchboxapp.screens.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.arch.fragment.BasePreferenceFragment
import app.atomofiron.common.util.Knife
import app.atomofiron.common.util.flow.viewCollect
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.bottom_sheet.BottomSheetView
import app.atomofiron.searchboxapp.screens.preferences.fragment.*
import app.atomofiron.searchboxapp.utils.Shell
import javax.inject.Inject
import kotlin.reflect.KClass

class PreferenceFragment : BasePreferenceFragment<PreferenceViewModel, PreferencePresenter>() {
    override val viewModelClass: KClass<PreferenceViewModel> = PreferenceViewModel::class

    @Inject
    override lateinit var presenter: PreferencePresenter

    private lateinit var exportImportDelegate: ExportImportFragmentDelegate
    private lateinit var explorerItemDelegate: ExplorerItemFragmentDelegate
    private lateinit var joystickDelegate: JoystickFragmentDelegate
    private lateinit var toyboxDelegate: ToyboxFragmentDelegate
    private lateinit var aboutDelegate: AboutFragmentDelegate
    private lateinit var preferenceDelegate: PreferenceFragmentDelegate

    private val bottomSheetView = Knife<BottomSheetView>(this, R.id.preference_bsv)

    override fun inject() = viewModel.inject(this)

    override fun onCreate() {
        preferenceDelegate = PreferenceFragmentDelegate(this, viewModel, presenter)
        addPreferencesFromResource(R.xml.preferences)

        exportImportDelegate = ExportImportFragmentDelegate(presenter)
        explorerItemDelegate = ExplorerItemFragmentDelegate(viewModel.explorerItemComposition, presenter)
        joystickDelegate = JoystickFragmentDelegate(viewModel.joystickComposition, presenter)
        toyboxDelegate = ToyboxFragmentDelegate(viewModel.toyboxVariant, presenter)
        aboutDelegate = AboutFragmentDelegate()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val viewGroup = inflater.inflate(R.layout.fragment_preference, container, false) as ViewGroup
        viewGroup.addView(view, 0)
        return viewGroup
    }

    override fun onCreateAdapter(preferenceScreen: PreferenceScreen): RecyclerView.Adapter<*> {
        preferenceDelegate.onUpdateScreen(preferenceScreen)
        return super.onCreateAdapter(preferenceScreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exportImportDelegate.bottomSheetView = bottomSheetView.view
        explorerItemDelegate.bottomSheetView = bottomSheetView.view
        joystickDelegate.bottomSheetView = bottomSheetView.view
        toyboxDelegate.bottomSheetView = bottomSheetView.view
        aboutDelegate.bottomSheetView = bottomSheetView.view

        onViewCollect()
    }

    override fun onCreateRecyclerView(inflater: LayoutInflater, parent: ViewGroup, savedInstanceState: Bundle?): RecyclerView {
        val recyclerView = super.onCreateRecyclerView(inflater, parent, savedInstanceState)
        recyclerView.clipToPadding = false
        val padding = resources.getDimensionPixelSize(R.dimen.joystick_size)
        recyclerView.setPadding(recyclerView.paddingLeft, recyclerView.paddingTop, recyclerView.paddingRight, padding)
        return recyclerView
    }

    private fun onViewCollect() = viewModel.apply {
        viewCollect(alert, ::showAlert)
        viewCollect(alertOutputSuccess, ::showOutputSuccess)
        viewCollect(alertOutputError, ::showOutputError)
    }

    override fun onBack(): Boolean = bottomSheetView(default = false) { hide() } || super.onBack()

    fun onAboutClick() = aboutDelegate.show()

    fun onExportImportClick() = exportImportDelegate.show()

    fun onExplorerItemClick() = explorerItemDelegate.show()

    fun onJoystickClick() = joystickDelegate.show()

    fun onToyboxClick() = toyboxDelegate.show()

    fun onLeakCanaryClick(isChecked: Boolean) = presenter.onLeakCanaryClick(isChecked)

    private fun showAlert(message: String) {
        Snackbar
                .make(thisView, message, Snackbar.LENGTH_SHORT)
                .setAnchorView(anchorView)
                .show()
    }

    private fun showOutputSuccess(message: Int) {
        val duration = when (message) {
            R.string.successful_with_restart -> Snackbar.LENGTH_LONG
            else -> Snackbar.LENGTH_SHORT
        }
        Snackbar.make(thisView, message, duration).setAnchorView(anchorView).show()
    }

    private fun showOutputError(output: Shell.Output) {
        Snackbar.make(thisView, R.string.error, Snackbar.LENGTH_SHORT)
                .apply {
                    if (output.error.isNotEmpty()) {
                        setAction(R.string.more) {
                            AlertDialog.Builder(context)
                                    .setMessage(output.error)
                                    .show()
                        }
                    }
                }
                .setAnchorView(anchorView)
                .show()
    }
}