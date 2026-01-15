package app.atomofiron.searchboxapp.screens.explorer

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.Alert
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.FragmentExplorerBinding
import app.atomofiron.searchboxapp.custom.ExplorerView
import app.atomofiron.searchboxapp.custom.LayoutDelegate.apply
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.other.LabeledAction
import app.atomofiron.searchboxapp.model.other.UniText
import app.atomofiron.searchboxapp.screens.common.delegates.apply
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerAlert
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerPagerAdapter
import app.atomofiron.searchboxapp.screens.explorer.state.ExplorerDock
import app.atomofiron.searchboxapp.screens.main.util.KeyCodeConsumer
import app.atomofiron.searchboxapp.utils.recyclerView
import app.atomofiron.searchboxapp.utils.showSnackbar

class ExplorerFragment : Fragment(R.layout.fragment_explorer),
    BaseFragment<ExplorerFragment, ExplorerViewState, ExplorerPresenter, FragmentExplorerBinding> by BaseFragmentImpl(),
    KeyCodeConsumer
{
    private lateinit var binding: FragmentExplorerBinding
    private lateinit var pagerAdapter: ExplorerPagerAdapter
    private val explorerViews get() = pagerAdapter.items

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, ExplorerViewModel::class, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentExplorerBinding.bind(view)
        pagerAdapter = ExplorerPagerAdapter(binding.pager, presenter)
        binding.initView()
        viewState.onViewCollect()
        binding.onApplyInsets()
    }

    private fun FragmentExplorerBinding.initView() {
        pager.adapter = pagerAdapter
        dockBar.setListener(::onNavigationItemSelected)
        pager.recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                presenter.onTabSelected(position)
            }
        })
    }

    private fun onNavigationItemSelected(item: DockItem) {
        when (item.id) {
            ExplorerDock.Search.id -> presenter.onSearchClick()
            ExplorerDock.Copy.id -> presenter.onCopyClick()
            ExplorerDock.PasteMove.id -> presenter.onPasteClick(move = true)
            ExplorerDock.PasteCopy.id -> presenter.onPasteClick(move = false)
            ExplorerDock.Settings.id -> presenter.onSettingsClick()
            ExplorerDock.Confirm.id -> presenter.onConfirmClick()
            in ExplorerDock.Sorting.children.ids() -> presenter.onSortPick(item)
        }
    }

    override fun ExplorerViewState.onViewCollect() {
        viewCollect(updates) {
            explorerViews.first().submit(it)
        }
        viewCollect(currentTabFlow) {
            val first = explorerViews.first()
            first.submit(it)
        }
        viewCollect(itemComposition) { composition ->
            explorerViews.forEach { it.setComposition(composition) }
        }
        viewCollect(scrollTo) { item ->
            getCurrentTabView().scrollTo(item)
        }
        viewCollect(alerts) { binding.snackbarContainer.showSnackbar(it, other = ::onAlert) }
        viewCollect(dock, collector = binding.dockBar::submit)
        viewCollect(currentTab) {
            binding.pager.currentItem = it.index
        }
        viewCollect(permissionRequiredWarning, collector = ::showPermissionRequiredWarning)
    }

    override fun FragmentExplorerBinding.onApplyInsets() {
        disclaimer.apply(viewState.mode, insetsBackground, root)
        root.apply(dockView = binding.dockBar, insetsBackground = insetsBackground)
    }

    override fun onBack(soft: Boolean): Boolean = presenter.onBack(soft, getCurrentTabView()::scrollToTop)

    override fun onStart() {
        super.onStart()
        explorerViews.forEach { it.onItemsVisible() }
    }

    override fun onKeyDown(keyCode: Int): Boolean = when {
        !isVisible -> false
        keyCode != KeyEvent.KEYCODE_VOLUME_UP -> false
        else -> getCurrentTabView().isDeepestDirVisible()?.also {
            presenter.onVolumeUp(it)
        } != null
    }

    private fun getCurrentTabView(): ExplorerView = explorerViews[binding.pager.currentItem]

    private fun onAlert(alert: ExplorerAlert): UniText {
        return when (alert) {
            is ExplorerAlert.Deleted -> alert.items.toUni(R.plurals.x_deleted, R.string.files_were_not_deleted)
            is ExplorerAlert.Copied -> alert.items.toUni(R.plurals.x_copied, R.string.files_were_not_copied)
            is ExplorerAlert.Moved -> alert.items.toUni(R.plurals.x_moved, R.string.files_were_not_moved)
        }
    }

    private fun List<Node>.toUni(@PluralsRes success: Int, @StringRes empty: Int): UniText {
        return takeIf { it.isNotEmpty() }?.let {
            val dirs = count { it.isDirectory }
            val files = size - dirs
            val what = listOfNotNull(
                resources.takeIf { dirs > 0 }?.getQuantityString(R.plurals.x_dirs, dirs, dirs),
                resources.takeIf { files > 0 }?.getQuantityString(R.plurals.x_files, files, files),
            ).joinToString(separator = resources.getString(R.string.and))
            resources.getQuantityString(success, size, what)
        }?.let { UniText(it) }
            ?: UniText(empty)
    }

    private fun showPermissionRequiredWarning(unit: Unit) {
        binding.snackbarContainer.showSnackbar(
            Alert(R.string.access_to_storage_forbidden),
            LabeledAction(R.string.allow) { presenter.onAllowStorageClick() },
        )
    }
}