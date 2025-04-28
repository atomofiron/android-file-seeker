package app.atomofiron.searchboxapp.screens.result

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.AlertMessage
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.common.util.unsafeLazy
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.FragmentResultBinding
import app.atomofiron.searchboxapp.custom.LayoutDelegate.apply
import app.atomofiron.searchboxapp.custom.addFastScroll
import app.atomofiron.searchboxapp.custom.view.dock.DockBarView
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.finder.SearchTask
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.result.adapter.ResultAdapter
import app.atomofiron.searchboxapp.utils.makeSnackbar
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.screens.result.state.ResultDockState.Companion.Default as DefaultDockState

class ResultFragment : Fragment(R.layout.fragment_result),
    BaseFragment<ResultFragment, ResultViewState, ResultPresenter, FragmentResultBinding> by BaseFragmentImpl()
{

    private lateinit var binding: FragmentResultBinding
    private lateinit var statusDrawable: Drawable

    private val resultAdapter = ResultAdapter()
    private val errorSnackbar by unsafeLazy {
        binding.snackbarContainer.makeSnackbar("", Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.got_it) { }
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
            addFastScroll()
        }
        binding.dockBar.submit(DefaultDockState)
        binding.dockBar.setListener(::onBottomMenuItemClick)
        viewState.onViewCollect()
        binding.onApplyInsets()
    }

    private fun onBottomMenuItemClick(item: DockItem) {
        when (item.id) {
            DefaultDockState.status.id -> presenter.onStopClick()
            DefaultDockState.sorting.id -> Unit
            DefaultDockState.export.id -> presenter.onExportClick()
            DefaultDockState.share.id -> presenter.onShareClick()
        }
    }

    override fun ResultViewState.onViewCollect() {
        viewCollect(composition, collector = ::onCompositionChange)
        viewCollect(task, collector = ::onTaskChange)
        viewCollect(alerts, collector = ::showSnackbar)
    }

    override fun FragmentResultBinding.onApplyInsets() {
        root.apply(recyclerView = recyclerView, dockView = dockBar, snackbarContainer = snackbarContainer)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        resultAdapter.notifyItemChanged(0)
    }

    private fun DockBarView.onTaskChange(task: SearchTask) {
        DefaultDockState.run {
            submit(copy(
                status = status.copy(enabled = task.inProgress),
                share = share.copy(enabled = !task.result.isEmpty),
                export = export.copy(enabled = !task.result.isEmpty),
            ))
        }
    }

    private fun onTaskChange(task: SearchTask) {
        binding.dockBar.onTaskChange(task)

        resultAdapter.setResult(task.result as SearchResult.FinderResult)

        if (!task.result.isEmpty) {
            // fix first item offset
            resultAdapter.notifyItemChanged(0)
        }
        if (task.error != null) {
            errorSnackbar.setText(task.error).show()
        }
        snackbarError = task.error
    }

    private fun onCompositionChange(composition: ExplorerItemComposition) {
        resultAdapter.setComposition(composition)
    }

    private fun showSnackbar(message: AlertMessage.Res) {
        val length = if (message.important) Snackbar.LENGTH_INDEFINITE else Snackbar.LENGTH_LONG
        binding.snackbarContainer.makeSnackbar(message.message, length)
            .apply { if (message.important) setAction(R.string.got_it) { } }
            .show()
    }
}