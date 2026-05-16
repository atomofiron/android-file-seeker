package app.atomofiron.searchboxapp.di.dependencies.delegate

import androidx.fragment.app.FragmentManager
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.searchboxapp.di.dependencies.channel.CommonChannel
import app.atomofiron.searchboxapp.model.other.AppScreen
import javax.inject.Inject

interface ScreenDelegate {
    fun watchScreens(manager: FragmentManager)
}

class ScreenDelegateImpl @Inject constructor(
    private val commonChannel: CommonChannel,
) : ScreenDelegate {

    override fun watchScreens(manager: FragmentManager) {
        manager.addOnBackStackChangedListener {
            val screen = manager.findDeeperOrLast(lastFound = AppScreen.Unknown)
            commonChannel.currentScreen.value = screen
        }
    }

    private fun FragmentManager.findDeeperOrLast(lastFound: AppScreen): AppScreen {
        val visible = fragments.findLast { it.isVisible }
            ?: return lastFound
        val lastFound = (visible as BaseFragment<*, *, *, *>)
            .screen ?: lastFound
        return visible.childFragmentManager.findDeeperOrLast(lastFound)
    }
}