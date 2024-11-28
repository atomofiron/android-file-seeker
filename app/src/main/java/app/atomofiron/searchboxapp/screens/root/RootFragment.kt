package app.atomofiron.searchboxapp.screens.root

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.LayoutDelegate.Companion.getLayout
import app.atomofiron.searchboxapp.databinding.FragmentRootBinding
import app.atomofiron.searchboxapp.screens.main.fragment.SnackbarCallbackFragmentDelegate.SnackbarCallbackOutput
import com.google.android.material.R as MaterialR

class RootFragment : Fragment(R.layout.fragment_root),
    BaseFragment<RootFragment, RootViewState, RootPresenter> by BaseFragmentImpl()
{

    private var sbListener: SnackbarCallbackOutput? = null

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
        applyToSnackbarContainer()
    }

    override fun onBack(): Boolean = presenter.onBack()

    private fun FragmentRootBinding.applyToSnackbarContainer() {
        val railSize = resources.getDimensionPixelSize(MaterialR.dimen.m3_navigation_rail_default_width)
        val bottomSize = resources.getDimensionPixelSize(MaterialR.dimen.m3_bottom_nav_min_height)
        ViewCompat.setOnApplyWindowInsetsListener(snackbarContainer) { container, windowInsets ->
            val layout = root.getLayout()
            val insets = windowInsets.getInsets(Type.systemBars() or Type.displayCutout())
            container.updatePadding(
                left = insets.left + if (layout.isLeft) railSize else 0,
                right = insets.right + if (layout.isRight) railSize else 0,
                bottom = insets.bottom + if (layout.isBottom) bottomSize else 0,
            )
            WindowInsetsCompat.CONSUMED
        }
    }
}