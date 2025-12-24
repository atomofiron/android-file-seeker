package app.atomofiron.searchboxapp.screens.root

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.fileseeker.R

class RootFragment : Fragment(R.layout.fragment_root),
    BaseFragment<RootFragment, RootViewState, RootPresenter, ViewBinding> by BaseFragmentImpl()
{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, RootViewModel::class, savedInstanceState)
    }
}