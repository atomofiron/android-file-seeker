package app.atomofiron.searchboxapp.screens.root

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.screens.explorer.ExplorerFragment
import app.atomofiron.searchboxapp.screens.finder.FinderFragment

class RootRouter(property: WeakProperty<out Fragment>) : BaseRouter(property) {

    override val currentDestinationId = R.id.rootFragment

    init {
        addFragments()
    }

    fun onBack(soft: Boolean): Boolean {
        fragment {
            childFragmentManager.run {
                fragments.findLastVisibleFragment()
                    ?.onBack(soft)
                    ?.takeIf { it }
                    ?.let { return true }
                if (backStackEntryCount > 0) {
                    popBackStack()
                    return true
                }
            }
        }
        return false
    }

    private fun addFragments() {
        fragment {
            if (childFragmentManager.fragments.isEmpty()) {
                val explorer = ExplorerFragment()
                val finder = FinderFragment()
                childFragmentManager.beginTransaction()
                    .add(R.id.root_fl_container, explorer)
                    .add(R.id.root_fl_container, finder)
                    .hide(finder)
                    .commit()
            }
        }
    }

    fun showSearch(show: Boolean) {
        fragment {
            if (show == (childFragmentManager.backStackEntryCount > 0)) {
                return
            }
            val fragment = childFragmentManager.fragments
                .find { it is FinderFragment }
                ?: return
            when {
                show -> childFragmentManager.beginTransaction()
                        .addToBackStack(null)
                        .show(fragment)
                        .commit()
                else -> childFragmentManager.popBackStack()
            }
        }
    }
}