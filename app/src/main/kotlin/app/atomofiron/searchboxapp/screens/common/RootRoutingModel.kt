package app.atomofiron.searchboxapp.screens.common

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

class RootRoutingModel : ViewModel() {
    companion object {
        operator fun invoke(anyFragment: Fragment) = ViewModelProvider(anyFragment.requireActivity())[RootRoutingModel::class.java]
    }

    @Inject
    lateinit var router: RootRouting

    fun showSearch() = router.showSearch()

    fun hideSearch() = router.hideSearch()
}

interface RootRouting {
    fun showSearch()
    fun hideSearch()
}
