package app.atomofiron.searchboxapp.screens.root

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

class RootRoutingModel : ViewModel() {
    companion object {
        operator fun invoke(anyFragment: Fragment) = ViewModelProvider(anyFragment.requireActivity())[RootRoutingModel::class.java]
    }

    @Inject
    lateinit var router: RootRouter

    fun showSearch() = router.showSearch(true)

    fun hideSearch() = router.showSearch(false)
}
