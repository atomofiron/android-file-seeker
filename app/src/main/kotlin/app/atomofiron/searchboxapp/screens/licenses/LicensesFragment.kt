package app.atomofiron.searchboxapp.screens.licenses

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.FragmentLicensesBinding
import app.atomofiron.searchboxapp.custom.LayoutDelegate.apply
import app.atomofiron.searchboxapp.screens.licenses.fragment.LicenseAdapter

class LicensesFragment : Fragment(R.layout.fragment_licenses)
    , BaseFragment<LicensesFragment, LicensesViewState, LicensesPresenter, ViewBinding> by BaseFragmentImpl()
{
    private lateinit var binding: FragmentLicensesBinding
    private val adapter = LicenseAdapter {
        presenter.onLicenseClick(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, LicensesViewModel::class, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLicensesBinding.bind(view).apply {
            toolbar.setNavigationOnClickListener { presenter.onNavigationClick() }
            recyclerView.adapter = adapter
            applyInsets()
        }
        viewState.onViewCollect()
    }

    override fun LicensesViewState.onViewCollect() {
        viewCollect(items) { adapter.submit(it) }
    }

    private fun FragmentLicensesBinding.applyInsets() {
        root.apply(recyclerView, appBarLayout = appbar)
    }
}