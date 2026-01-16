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
import app.atomofiron.common.util.Alert
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.searchboxapp.utils.colorAttr
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.FragmentPreferenceBinding
import app.atomofiron.searchboxapp.custom.LayoutDelegate.addLayoutListener
import app.atomofiron.searchboxapp.custom.LayoutDelegate.apply
import app.atomofiron.searchboxapp.custom.overscroll.setupSpringOverscroll
import app.atomofiron.searchboxapp.custom.preference.AppUpdatePreference
import app.atomofiron.searchboxapp.custom.preference.DropDownPreference
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceFragmentDelegate
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.performHapticLite
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys
import app.atomofiron.searchboxapp.utils.showSnackbar
import com.google.android.material.appbar.AppBarLayout
import lib.atomofiron.insets.insetsPadding

class PreferenceFragment : PreferenceFragmentCompat(),
    BaseFragment<PreferenceFragment, PreferenceViewState, PreferencePresenter, FragmentPreferenceBinding> by BaseFragmentImpl()
{
    private lateinit var preferenceDelegate: PreferenceFragmentDelegate
    private lateinit var binding: FragmentPreferenceBinding

    private lateinit var joystickPreference: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        initViewModel(this, PreferenceViewModel::class, savedInstanceState)

        preferenceManager.preferenceDataStore = viewState.preferenceDataStore
        preferenceDelegate = PreferenceFragmentDelegate(this, resources, viewState, presenter)
        setPreferencesFromResource(R.xml.preferences, rootKey)
        preferenceDelegate.onCreatePreference(preferenceScreen)

        val deepBlack = findPreference<Preference>(PreferenceKeys.KeyDeepBlack.name)
        viewState.showDeepBlack.collect(lifecycleScope) {
            deepBlack.isVisible = it
        }
        val hapticFeedback = findPreference<SwitchPreferenceCompat>(PreferenceKeys.KeyHapticFeedback.name)
        viewState.hapticFeedback.collect(lifecycleScope) {
            hapticFeedback.isChecked = it
        }
        val screenshotOps = findPreference<SwitchPreferenceCompat>(PreferenceKeys.KeyScreenshotOperations.name)
        viewState.screenshotOpsError.collect(lifecycleScope) {
            screenshotOps.summary = it
        }
        val uppUpdate = findPreference<AppUpdatePreference>(PreferenceKeys.PREF_APP_UPDATE)
        uppUpdate.listener = presenter
        viewState.appUpdate.collect(lifecycleScope) {
            uppUpdate.bind(it)
        }
        val asSu = findPreference<SwitchPreferenceCompat>(PreferenceKeys.KeyUseSu.name)
        viewState.asSu.collect(lifecycleScope) {
            asSu.isChecked = it
        }
        val debugGroup = findPreference<PreferenceGroup>(PreferenceKeys.PREF_CATEGORY_DEBUG)
        debugGroup.isVisible = viewState.withDebugGroup

        joystickPreference = findPreference(PreferenceKeys.KeyJoystick.name)
        if (Android.T) findPreference<DropDownPreference>(PreferenceKeys.KeyLocale.name).interceptClicks()
    }

    override fun <T : Preference> findPreference(key: CharSequence): T = super.findPreference(key)!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentPreferenceBinding.inflate(inflater, container, false)
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        (recyclerView.parent as ViewGroup).removeView(recyclerView)
        recyclerView.removeItemDecorationAt(0)
        recyclerView.setupSpringOverscroll()
        recyclerView.isVerticalScrollBarEnabled = false
        recyclerView.layoutParams = CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
            behavior = AppBarLayout.ScrollingViewBehavior()
        }
        binding.root.findViewById<RecyclerView>(R.id.recycler_view)
            .let { binding.root.indexOfChild(it) }
            .also { binding.root.removeViewAt(it) }
            .also { binding.root.addView(recyclerView, it) }
        binding.root.addLayoutListener {
            joystickPreference.isVisible = it.withJoystick
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPreferenceBinding.bind(view)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        view.setBackgroundColor(view.context.colorAttr(R.attr.colorBackground))
        preferenceScreen.fixIcons()
        recyclerView.clipToPadding = false
        recyclerView.updatePadding(top = resources.getDimensionPixelSize(R.dimen.content_margin_half))
        binding.toolbar.setNavigationOnClickListener { presenter.onNavigationClick() }
        binding.toolbar.setOnMenuItemClickListener { item ->
            view.performHapticLite()
            when (item.itemId) {
                R.id.pref_about -> presenter.onAboutClick()
            }
            true
        }
        binding.onApplyInsets()
        viewState.onViewCollect()
    }

    override fun FragmentPreferenceBinding.onApplyInsets() {
        root.apply(recyclerView, header = header, insetsBackground = insetsBackground)
    }

    override fun PreferenceViewState.onViewCollect() {
        viewCollect(alerts, collector = ::onAlert)
    }

    private fun PreferenceGroup.fixIcons() {
        // todo foresee NoticeableDrawable and colored icons
        val iconTint = requireContext().colorAttr(MaterialAttr.colorControlNormal)
        forEach {
            it.icon?.setTint(iconTint)
            if (it is PreferenceGroup) it.fixIcons()
        }
    }

    private fun onAlert(alert: Alert) = binding.snackbarContainer.showSnackbar(alert)
}