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
        val consumed = fragment {
            val lastVisibleFragment = childFragmentManager.fragments.findLastVisibleFragment()
            val consumed = lastVisibleFragment?.onBack(soft) ?: false
            consumed || (lastVisibleFragment !is ExplorerFragment).also { explorer ->
                if (explorer) childFragmentManager.switchScreen { it is ExplorerFragment }
            }
        }
        return consumed ?: false
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
}