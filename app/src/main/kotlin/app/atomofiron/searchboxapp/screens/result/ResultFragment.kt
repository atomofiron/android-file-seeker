package app.atomofiron.searchboxapp.screens.result

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.common.util.unsafeLazy
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.FragmentResultBinding
import app.atomofiron.searchboxapp.custom.LayoutDelegate.apply
import app.atomofiron.searchboxapp.custom.addExplorerFastScroll
import app.atomofiron.searchboxapp.custom.overscroll.setupSpringOverscroll
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.screens.common.delegates.apply
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator.ItemBackgroundDecorator
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.TAG_EXPLORER_OPENED_ITEM
import app.atomofiron.searchboxapp.screens.result.adapter.ItemGravityDecorator
import app.atomofiron.searchboxapp.screens.result.adapter.ResultAdapter
import app.atomofiron.searchboxapp.utils.showSnackbar
import app.atomofiron.searchboxapp.screens.result.state.ResultDockState.Companion.Default as DefaultDockState

class ResultFragment : Fragment(R.layout.fragment_result),
    BaseFragment<ResultFragment, ResultViewState, ResultPresenter, FragmentResultBinding> by BaseFragmentImpl()
{
    private lateinit var binding: FragmentResultBinding
    private lateinit var statusDrawable: Drawable

    private val resultAdapter = ResultAdapter()
    private val gravityDecorator = ItemGravityDecorator()
    private val backgroundDecorator by unsafeLazy {
        ItemBackgroundDecorator(R.id.item_explorer, evenNumbered = false, ignoringTag = TAG_EXPLORER_OPENED_ITEM, resources)
    }

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
            addExplorerFastScroll()
            setupSpringOverscroll()
            addItemDecoration(gravityDecorator)
            addItemDecoration(backgroundDecorator)
        }
        binding.dockBar.submit(DefaultDockState)
        binding.dockBar.setListener(::onBottomMenuItemClick)
        viewState.onViewCollect()
        binding.onApplyInsets()
    }

    private fun onBottomMenuItemClick(item: DockItem) {
        when (val id = item.id) {
            DefaultDockState.sorting.id -> Unit
            DefaultDockState.status.id -> presenter.onStopClick()
            DefaultDockState.export?.id -> presenter.onExportClick()
            DefaultDockState.confirm?.id -> presenter.onConfirmClick()
            DefaultDockState.share.id -> presenter.onShareClick()
            is NodeSorting -> presenter.onSortingSelected(id)
        }
    }

    override fun ResultViewState.onViewCollect() {
        viewCollect(composition) {
            backgroundDecorator.enabled = it.visibleBg
            resultAdapter.setComposition(it)
        }
        viewCollect(items) { resultAdapter.submit(it) }
        viewCollect(isReady) { binding.progress.isGone = it }
        viewCollect(updates) { resultAdapter.submit(it) }
        viewCollect(alerts) { binding.snackbarContainer.showSnackbar(it) }
        viewCollect(dock, collector = binding.dockBar::submit)
    }

    override fun FragmentResultBinding.onApplyInsets() {
        disclaimer.apply(viewState.mode, insetsBackground, root)
        root.apply(recyclerView = recyclerView, dockView = dockBar, insetsBackground = insetsBackground)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        resultAdapter.notifyItemChanged(0)
    }
}