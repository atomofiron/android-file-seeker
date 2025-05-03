package app.atomofiron.searchboxapp.screens.root

import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseRouter
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.injectable.delegate.InitialDelegate
import app.atomofiron.searchboxapp.model.other.InitialScreen
import app.atomofiron.searchboxapp.screens.explorer.ExplorerFragment
import app.atomofiron.searchboxapp.screens.finder.FinderFragment

class RootRouter(
    property: WeakProperty<out Fragment>,
    private val initialDelegate: InitialDelegate,
) : BaseRouter(property) {

    override val currentDestinationId = R.id.rootFragment

    private val initialScreen get() = initialDelegate.initialScreen()

    init {
        addFragments()
    }

    fun checkCurrentScreen() = when {
        currentDestinationId != R.id.explorerFragment -> Unit
        initialScreen == InitialScreen.Explorer -> Unit
        else -> fragment {
            childFragmentManager.switchScreen { it is FinderFragment }
        }
    }

    override fun navigateBack(): Boolean {
        val consumed = fragment {
            val lastVisibleFragment = childFragmentManager.fragments.findLastVisibleFragment()
            var consumed = lastVisibleFragment?.onBack(soft = false) ?: false
            if (!consumed && lastVisibleFragment?.let { !isInitialFragment(it) } == true) {
                childFragmentManager.switchScreen { isInitialFragment(it) }
                consumed = true
            }
            consumed
        }
        return consumed ?: false
    }

    private fun addFragments() {
        fragment {
            if (childFragmentManager.fragments.isEmpty()) {
                val explorer = ExplorerFragment()
                val finder = FinderFragment()
                val (initial, second) = when {
                    isInitialFragment(explorer) -> explorer to finder
                    else -> finder to explorer
                }
                childFragmentManager.beginTransaction()
                    .add(R.id.root_fl_container, initial)
                    .add(R.id.root_fl_container, second)
                    .hide(second)
                    .commit()
            }
        }
    }

    private fun isInitialFragment(fragment: Any): Boolean {
        return when (initialScreen) {
            InitialScreen.Explorer -> fragment is ExplorerFragment
            InitialScreen.Search -> fragment is FinderFragment
        }
    }
}