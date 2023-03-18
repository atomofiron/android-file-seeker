package app.atomofiron.searchboxapp.screens.result

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.flow.viewCollect
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.anchorView
import app.atomofiron.searchboxapp.custom.OrientationLayoutDelegate
import app.atomofiron.searchboxapp.databinding.FragmentResultBinding
import app.atomofiron.searchboxapp.model.finder.FinderTask
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.result.adapter.ResultAdapter
import app.atomofiron.searchboxapp.setContentMaxWidthRes
import app.atomofiron.searchboxapp.utils.setProgressItem
import app.atomofiron.searchboxapp.utils.setState
import lib.atomofiron.android_window_insets_compat.applyPaddingInsets
import lib.atomofiron.android_window_insets_compat.insetsProxying

class ResultFragment : Fragment(R.layout.fragment_result),
    BaseFragment<ResultFragment, ResultViewState, ResultPresenter> by BaseFragmentImpl()
{

    private lateinit var binding: FragmentResultBinding
    private lateinit var statusDrawable: Drawable

    private val resultAdapter = ResultAdapter()
    private val errorSnackbar by lazy(LazyThreadSafetyMode.NONE) {
        Snackbar.make(requireView(), "", Snackbar.LENGTH_INDEFINITE)
            .setAnchorView(anchorView)
            .setAction(R.string.dismiss) { presenter.onDropTaskErrorClick() }
    }
    private var snackbarError: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, ResultViewModel::class, savedInstanceState)

        resultAdapter.itemActionListener = presenter
        statusDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_search_status)!!
        statusDrawable.setTintList(ContextCompat.getColorStateList(requireContext(), R.color.ic_search_status))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentResultBinding.bind(view)

        binding.recyclerView.run {
            itemAnimator = null
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resultAdapter
        }
        binding.bottomBar.setContentMaxWidthRes(R.dimen.bottom_bar_max_width)
        binding.bottomBar.isItemActiveIndicatorEnabled = false
        binding.bottomBar.setOnItemSelectedListener(::onBottomMenuItemClick)
        viewState.onViewCollect()
        onApplyInsets(view)
    }

    private fun onBottomMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_stop -> presenter.onStopClick()
            R.id.menu_export -> presenter.onExportClick()
        }
        return false
    }

    override fun ResultViewState.onViewCollect() {
        viewCollect(composition, collector = ::onCompositionChange)
        viewCollect(task, collector = ::onTaskChange)
        viewCollect(enableOptions, collector = ::enableOptions)
        viewCollect(notifyTaskHasChanged) { resultAdapter.notifyDataSetChanged() }
    }

    override fun onApplyInsets(root: View) {
        binding.recyclerView.applyPaddingInsets()
        root.insetsProxying()
        binding.recyclerView.applyPaddingInsets()
        OrientationLayoutDelegate(
            root as ViewGroup,
            recyclerView = binding.recyclerView,
            bottomView = binding.bottomBar,
            railView = null,
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        resultAdapter.notifyItemChanged(0)
    }

    @SuppressLint("RestrictedApi")
    private fun onTaskChange(task: FinderTask) {
        binding.bottomBar.run {
            val label = "${task.results.size}/${task.count}"
            val enabled = task.error == null
            when {
                task.inProgress -> setProgressItem(R.id.menu_progress, R.drawable.progress_loop, label, enabled)
                else -> {
                    statusDrawable.setState(enabled = enabled, activated = task.isDone)
                    setProgressItem(R.id.menu_progress, statusDrawable, label, enabled)
                }
            }
            var item = menu.findItem(R.id.menu_stop)
            if (item.isEnabled != task.inProgress) {
                item.isEnabled = task.inProgress
            }
            item = menu.findItem(R.id.menu_export)
            if (item.isEnabled != task.results.isNotEmpty()) {
                item.isEnabled = task.results.isNotEmpty()
            }
        }
        resultAdapter.setResults(task.results)

        if (task.results.isNotEmpty()) {
            // fix first item offset
            resultAdapter.notifyItemChanged(0)
        }

        if (task.error != snackbarError) {
            errorSnackbar.setText(task.error!!).show()
        }
    }

    private fun onCompositionChange(composition: ExplorerItemComposition) {
        resultAdapter.setComposition(composition)
    }

    @SuppressLint("RestrictedApi")
    private fun enableOptions(enable: Boolean) {
        // todo resolve
        /*val item = binding.bottomBar.menu.findItem(R.id.menu_options)
        if (item.isEnabled != enable) {
            item.isEnabled = enable
        }*/
    }
}