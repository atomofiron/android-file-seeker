package app.atomofiron.searchboxapp.screens.template

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.fileseeker.R

class TemplateFragment : Fragment(R.layout.fragment_template),
    BaseFragment<TemplateFragment, TemplateViewState, TemplatePresenter, ViewBinding> by BaseFragmentImpl()
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, TemplateViewModel::class, savedInstanceState)
    }
}