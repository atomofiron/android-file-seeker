package app.atomofiron.searchboxapp.screens.finder

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.flow.viewCollect
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.FragmentFinderBinding
import app.atomofiron.searchboxapp.custom.LayoutDelegate.apply
import app.atomofiron.searchboxapp.custom.drawable.NoticeableDrawable
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapter
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderSpanSizeLookup
import app.atomofiron.searchboxapp.screens.finder.history.adapter.HistoryAdapter
import app.atomofiron.searchboxapp.screens.finder.state.FinderDock
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.state.finderDockItems
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.makeSnackbar
import app.atomofiron.searchboxapp.utils.set

class FinderFragment : Fragment(R.layout.fragment_finder),
    BaseFragment<FinderFragment, FinderViewState, FinderPresenter, FragmentFinderBinding> by BaseFragmentImpl()
{

    private lateinit var binding: FragmentFinderBinding
    private val finderAdapter = FinderAdapter()
    private lateinit var layoutManager: GridLayoutManager

    private val historyAdapter: HistoryAdapter by lazy {
        HistoryAdapter(requireContext(), object : HistoryAdapter.OnItemClickListener {
            override fun onItemClick(node: String) {
                binding.drawer.close()
                presenter.onHistoryItemClick(node)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, FinderViewModel::class, savedInstanceState)

        finderAdapter.output = presenter
        layoutManager = GridLayoutManager(context, 1)
        layoutManager.spanSizeLookup = FinderSpanSizeLookup(finderAdapter, layoutManager)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentFinderBinding.bind(view)

        binding.recyclerView.run {
            this@FinderFragment.layoutManager.reverseLayout = true
            layoutManager = this@FinderFragment.layoutManager
            itemAnimator = null
            adapter = finderAdapter
        }

        binding.dockBar.submit(finderDockItems(NoticeableDrawable(requireContext(), R.drawable.ic_settings)))
        binding.dockBar.setListener(::onNavigationItemSelected)

        binding.drawer.run {
            onGravityChangeListener = presenter::onDrawerGravityChange
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = historyAdapter
        }

        val columnWidth = resources.getDimensionPixelSize(R.dimen.finder_column_width)
        binding.recyclerView.addOnLayoutChangeListener { recyclerView, left, _, right, _, _, _, _, _ ->
            val width = right - left - recyclerView.paddingStart - recyclerView.paddingEnd
            layoutManager.spanCount = (width / columnWidth).coerceAtLeast(1)
        }

        viewState.onViewCollect()
        binding.onApplyInsets()
    }

    private fun onNavigationItemSelected(item: DockItem) {
        when (item.id) {
            FinderDock.Files -> presenter.onExplorerOptionSelected()
            FinderDock.Settings -> presenter.onSettingsOptionSelected()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (hidden) {
            view?.let {
                val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                inputMethodManager?.hideSoftInputFromWindow(it.windowToken, 0)
            }
        }
    }

    override fun FinderViewState.onViewCollect() {
        viewCollect(historyDrawerGravity) { binding.drawer.gravity = it }
        viewCollect(reloadHistory, collector = historyAdapter::reload)
        viewCollect(history, collector = historyAdapter::add)
        viewCollect(insertInQuery, collector = ::onInsertInQuery)
        viewCollect(searchItems, collector = ::onStateChange)
        viewCollect(replaceQuery, collector = ::onReplaceQuery)
        viewCollect(snackbar, collector = ::onShowSnackbar)
        viewCollect(showHistory) { binding.drawer.open() }
        viewCollect(permissionRequiredWarning, collector = ::showPermissionRequiredWarning)
        viewCollect(settingsNotification) {
            binding.dockBar[FinderDock.Settings] = it
        }
    }

    override fun FragmentFinderBinding.onApplyInsets() {
        root.apply(recyclerView = recyclerView, dockView = dockBar)
        insetsBackground.setAdditional(ExtType.dock)
    }

    override fun onBack(soft: Boolean): Boolean {
        val consumed = binding.drawer.isOpened
        binding.drawer.close()
        return consumed || super.onBack(soft)
    }

    private fun onStateChange(items: List<FinderStateItem>) = finderAdapter.submitList(items)

    private fun onReplaceQuery(value: String) {
        view?.findViewById<EditText>(R.id.item_find_rt_find)?.setText(value)
    }

    private fun onShowSnackbar(value: String) {
        binding.snackbarContainer.makeSnackbar(value, Snackbar.LENGTH_SHORT).show()
    }

    private fun onInsertInQuery(value: String) {
        view?.findViewById<EditText>(R.id.item_find_rt_find)
                ?.takeIf { it.isFocused }
                ?.apply {
                    text.replace(selectionStart, selectionEnd, value)
                }
    }

    private fun showPermissionRequiredWarning(unit: Unit) {
        binding.snackbarContainer.makeSnackbar(R.string.access_to_storage_forbidden, Snackbar.LENGTH_LONG)
            .setAction(R.string.allow) { presenter.onAllowStorageClick() }
            .show()
    }
}