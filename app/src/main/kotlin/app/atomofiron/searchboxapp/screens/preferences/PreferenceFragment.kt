package app.atomofiron.searchboxapp.screens.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.forEach
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.searchboxapp.MaterialAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.preference.AppUpdatePreference
import app.atomofiron.searchboxapp.screens.preferences.fragment.PreferenceFragmentDelegate
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.PreferenceKeys
import app.atomofiron.searchboxapp.utils.Shell
import app.atomofiron.searchboxapp.utils.anchorView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.snackbar.Snackbar
import lib.atomofiron.insets.insetsPadding

class PreferenceFragment : PreferenceFragmentCompat(),
    BaseFragment<PreferenceFragment, PreferenceViewState, PreferencePresenter> by BaseFragmentImpl()
{
    private lateinit var preferenceDelegate: PreferenceFragmentDelegate

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
        val uppUpdate = findPreference<AppUpdatePreference>(PreferenceKeys.KeyAppUpdate.name)!!
        uppUpdate.listener = presenter
        viewState.appUpdate.collect(lifecycleScope) {
            uppUpdate.bind(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.fragment_preference, container, false)
        root as ViewGroup
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val listContainer = view.findViewById<FrameLayout>(android.R.id.list_container)
        listContainer.removeView(recyclerView)
        recyclerView.isVerticalScrollBarEnabled = false
        recyclerView.layoutParams = CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
            behavior = AppBarLayout.ScrollingViewBehavior()
        }
        root.addView(recyclerView, 1)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbarLayout = view.findViewById<CollapsingToolbarLayout>(R.id.collapsing_layout)
        val appBarLayout = view.findViewById<AppBarLayout>(R.id.appbar_layout)
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        view.setBackgroundColor(view.context.findColorByAttr(R.attr.colorBackground))
        preferenceScreen.fixIcons()
        recyclerView.clipToPadding = false
        recyclerView.updatePadding(top = resources.getDimensionPixelSize(R.dimen.content_margin_half))
        toolbar.setNavigationOnClickListener { presenter.onNavigationClick() }
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.pref_about -> presenter.onAboutClick()
            }
            true
        }
        recyclerView?.insetsPadding(ExtType { barsWithCutout + joystickBottom + joystickFlank }, start = true, end = true, bottom = true)
        appBarLayout?.insetsPadding(ExtType { barsWithCutout + joystickFlank }, top = true)
        toolbarLayout?.insetsPadding(ExtType { barsWithCutout + joystickFlank }, start = true, end = true)
        viewState.onViewCollect()
    }

    override fun PreferenceViewState.onViewCollect() {
        viewCollect(alert, collector = ::onAlert)
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
        val view = view ?: return
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
            .setAnchorView(anchorView)
            .show()
    }

    private fun showOutputSuccess(message: Int) {
        val view = view ?: return
        val duration = when (message) {
            R.string.successful_with_restart -> Snackbar.LENGTH_LONG
            else -> Snackbar.LENGTH_SHORT
        }
        Snackbar.make(view, message, duration).setAnchorView(anchorView).show()
    }

    private fun showOutputError(output: Shell.Output) {
        val view = view ?: return
        val anchorView = anchorView
        Snackbar.make(view, R.string.error, Snackbar.LENGTH_SHORT).apply {
            if (output.error.isNotEmpty()) {
                setAction(R.string.more) {
                    AlertDialog.Builder(context)
                            .setMessage(output.error)
                            .show()
                }
            }
            this.anchorView = anchorView
            show()
        }
    }
}