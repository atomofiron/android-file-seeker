package app.atomofiron.searchboxapp.screens.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.navigation.CustomNavHostFragment
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.fileseeker.R

class MainRouter(activityProperty: WeakProperty<out FragmentActivity>) : BaseRouter(activityProperty) {

    override val currentDestinationId = 0
    override val isCurrentDestination: Boolean = true

    private val fragmentManager: FragmentManager? get() = activity?.supportFragmentManager
        ?.fragments
        ?.firstOrNull()
        ?.let {
            it as CustomNavHostFragment
        }?.childFragmentManager

    private val fragments: List<Fragment>? get() = fragmentManager?.fragments

    val lastVisibleFragment get() = fragments.findLastVisibleFragment()

    fun recreateActivity() {
        activity {
            recreate()
        }
    }

    fun showSettings() = navigate(R.id.preferenceFragment)

    fun onBack(soft: Boolean): Boolean {
        val lastVisibleFragment = lastVisibleFragment
        val consumed = lastVisibleFragment?.onBack(soft) == true
        return consumed || navigation {
            navigateUp()
        } ?: false
    }
}