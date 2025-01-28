package app.atomofiron.searchboxapp.screens.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.SwitchPreferenceCompat
import androidx.preference.forEach
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.searchboxapp.MaterialAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.preference.AppUpdatePreference
import app.atomofiron.fileseeker.databinding.FragmentPreferenceBinding
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceFragmentDelegate
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.Shell
import app.atomofiron.searchboxapp.utils.isRtl
import app.atomofiron.searchboxapp.utils.makeSnackbar
import app.atomofiron.searchboxapp.utils.prederences.PreferenceKeys
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import lib.atomofiron.insets.ExtendedWindowInsets
import lib.atomofiron.insets.InsetsListener
import lib.atomofiron.insets.attachInsetsListener
import lib.atomofiron.insets.insetsPadding

class PreferenceFragment : PreferenceFragmentCompat(),
    BaseFragment<PreferenceFragment, PreferenceViewState, PreferencePresenter> by BaseFragmentImpl()
{
    private lateinit var preferenceDelegate: PreferenceFragmentDelegate
    private lateinit var binding: FragmentPreferenceBinding

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        initViewModel(this, PreferenceViewModel::class, savedInstanceState)

        preferenceManager.preferenceDataStore = viewState.preferenceDataStore
        preferenceDelegate = PreferenceFragmentDelegate(resources, viewState, presenter)
        setPreferencesFromResource(R.xml.preferences, rootKey)
        preferenceDelegate.onCreatePreference(preferenceScreen)

        val deepBlack = findPreference<Preference>(PreferenceKeys.KeyDeepBlack.name)!!
        viewState.showDeepBlack.collect(lifecycleScope) {
            deepBlack.isVisible = it
        }
        val uppUpdate = findPreference<AppUpdatePreference>(PreferenceKeys.PREF_APP_UPDATE)!!
        uppUpdate.listener = presenter
        viewState.appUpdate.collect(lifecycleScope) {
            uppUpdate.bind(it)
        }
        val useSu = findPreference<SwitchPreferenceCompat>(PreferenceKeys.KeyUseSu.name)!!
        viewState.useSu.collect(lifecycleScope) {
            useSu.isChecked = it
        }
        val debugGroup = findPreference<PreferenceGroup>(PreferenceKeys.PREF_CATEGORY_DEBUG)!!
        debugGroup.isVisible = viewState.withDebugGroup
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_preference, container, false)
        root as ViewGroup
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        (recyclerView.parent as ViewGroup).removeView(recyclerView)
        recyclerView.isVerticalScrollBarEnabled = false
        recyclerView.layoutParams = CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
            behavior = AppBarLayout.ScrollingViewBehavior()
        }
        root.addView(recyclerView, 1)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPreferenceBinding.bind(view)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        view.setBackgroundColor(view.context.findColorByAttr(R.attr.colorBackground))
        preferenceScreen.fixIcons()
        recyclerView.clipToPadding = false
        recyclerView.updatePadding(top = resources.getDimensionPixelSize(R.dimen.content_margin_half))
        binding.toolbar.setNavigationOnClickListener { presenter.onNavigationClick() }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.pref_about -> presenter.onAboutClick()
            }
            true
        }
        recyclerView?.insetsPadding(ExtType { barsWithCutout + ime + joystickBottom + joystickFlank }, start = true, end = true, bottom = true)
        binding.appbarLayout.insetsPadding(ExtType { barsWithCutout + joystickFlank }, top = true)
        binding.toolbar.insetsPadding(ExtType { barsWithCutout + joystickFlank }, start = true, end = true)
        binding.collapsingLayout.fixInsets()
        viewState.onViewCollect()
    }

    override fun PreferenceViewState.onViewCollect() {
        viewCollect(alerts, collector = ::onAlert)
        viewCollect(alertOutputSuccess, collector = ::showOutputSuccess)
        viewCollect(alertOutputError, collector = ::showOutputError)
    }

    private fun PreferenceGroup.fixIcons() {
        // todo foresee NoticeableDrawable and colored icons
        val iconTint = requireContext().findColorByAttr(MaterialAttr.colorControlNormal)
        forEach {
            it.icon?.setTint(iconTint)
            if (it is PreferenceGroup) it.fixIcons()
        }
    }

    private fun onAlert(message: String) {
        binding.snackbarContainer.makeSnackbar(message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showOutputSuccess(message: Int) {
        val duration = when (message) {
            R.string.successful_with_restart -> Snackbar.LENGTH_LONG
            else -> Snackbar.LENGTH_SHORT
        }
        binding.snackbarContainer.makeSnackbar(message, duration).show()
    }

    private fun showOutputError(output: Shell.Output) {
        binding.snackbarContainer.makeSnackbar(R.string.error, Snackbar.LENGTH_SHORT).apply {
            if (output.error.isNotEmpty()) {
                setAction(R.string.more) {
                    MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.error)
                        .setMessage(output.error)
                        .setPositiveButton(R.string.ok) { _, _ -> }
                        .show()
                }
            }
            show()
        }
    }

    private fun CollapsingToolbarLayout.fixInsets() = object : InsetsListener {
        private val isRtl = isRtl()
        private val defaultStart = expandedTitleMarginStart
        private val defaultEnd = expandedTitleMarginEnd
        override val types = ExtType { barsWithCutout + joystickFlank }
        override fun onApplyWindowInsets(windowInsets: ExtendedWindowInsets) {
            val insets = windowInsets[types]
            expandedTitleMarginStart = defaultStart + if (isRtl) insets.right else insets.left
            expandedTitleMarginEnd = defaultEnd + if (isRtl) insets.left else insets.right
        }
    }.let { binding.root.attachInsetsListener(it) }
}