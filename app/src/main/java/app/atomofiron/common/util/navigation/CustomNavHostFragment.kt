package app.atomofiron.common.util.navigation

import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavHostController
import androidx.navigation.fragment.DialogFragmentNavigator
import app.atomofiron.common.util.navigation.CustomFragmentNavigator

class CustomNavHostFragment : NavHostFragment() {

    override fun onCreateNavHostController(navHostController: NavHostController) {
        super.onCreateNavHostController(navHostController)
        val provider = navHostController.navigatorProvider
        provider.addNavigator(DialogFragmentNavigator(requireContext(), childFragmentManager))
        provider.addNavigator(CustomFragmentNavigator(requireContext(), childFragmentManager, id))
    }
}