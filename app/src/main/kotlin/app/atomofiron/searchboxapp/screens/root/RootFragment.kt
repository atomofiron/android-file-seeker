package app.atomofiron.searchboxapp.screens.root

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.FragmentRootBinding
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.insetsPadding

class RootFragment : Fragment(R.layout.fragment_root),
    BaseFragment<RootFragment, RootViewState, RootPresenter> by BaseFragmentImpl()
{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, RootViewModel::class, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentRootBinding.bind(view)
        binding.applyInsets()
        presenter.onChildrenCreated()
    }

    private fun FragmentRootBinding.applyInsets() {
        snackbarContainer.insetsPadding(ExtType { barsWithCutout + navigation + rail })
    }

    override fun onBack(): Boolean = presenter.onBack()
}