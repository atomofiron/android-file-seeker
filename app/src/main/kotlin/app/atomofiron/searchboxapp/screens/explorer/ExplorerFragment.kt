package app.atomofiron.searchboxapp.screens.explorer

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.Insets
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.AlertMessage
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.FragmentExplorerBinding
import app.atomofiron.searchboxapp.custom.ExplorerView
import app.atomofiron.searchboxapp.custom.LayoutDelegate.apply
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerAlert
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerPagerAdapter
import app.atomofiron.searchboxapp.screens.explorer.state.ExplorerDock
import app.atomofiron.searchboxapp.screens.main.util.KeyCodeConsumer
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.getString
import app.atomofiron.searchboxapp.utils.makeSnackbar
import app.atomofiron.searchboxapp.utils.recyclerView
import com.google.android.material.snackbar.Snackbar
import lib.atomofiron.insets.InsetsSource
import lib.atomofiron.insets.attachInsetsListener
import lib.atomofiron.insets.insetsPadding
import lib.atomofiron.insets.insetsSource

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
        when (viewState.mode) {
            ActivityMode.Default -> binding.disclaimer.isVisible = false
            is ActivityMode.Share -> binding.disclaimer.setText(R.string.disclaimer_pick_files)
            is ActivityMode.Receive -> binding.disclaimer.setText(R.string.disclaimer_choose_directory)
        }
    }

    private fun onNavigationItemSelected(item: DockItem) {
        when (item.id as ExplorerDock) {
            ExplorerDock.Search -> presenter.onSearchClick()
            ExplorerDock.Settings -> presenter.onSettingsClick()
            ExplorerDock.Confirm -> presenter.onConfirmClick()
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
        viewCollect(alerts, collector = ::showSnackbar)
        viewCollect(dock, collector = binding.dockBar::submit)
        viewCollect(currentTab) {
            binding.pager.currentItem = it.index
        }
        viewCollect(permissionRequiredWarning, collector = ::showPermissionRequiredWarning)
    }

    override fun FragmentExplorerBinding.onApplyInsets() {
        root.apply(dockView = binding.dockBar)
        systemUiBackground += ExtType.topDisclaimer
        root.attachInsetsListener(systemUiBackground)
        disclaimer.insetsPadding(ExtType { barsWithCutout + dock }, start = true, top = true, end = true)
        disclaimer.insetsSource {
            InsetsSource.submit(ExtType.topDisclaimer, Insets.of(0, it.height, 0, 0))
        }
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

    private fun showSnackbar(message: AlertMessage) {
        binding.snackbarContainer.run {
            when (message) {
                is AlertMessage.Str -> makeSnackbar(message.message, Snackbar.LENGTH_LONG)
                is AlertMessage.Res -> makeSnackbar(message.message, Snackbar.LENGTH_LONG)
                is AlertMessage.Other<*> -> when (message.message) {
                    is NodeError -> makeSnackbar(resources.getString(message.message), Snackbar.LENGTH_LONG)
                    is ExplorerAlert.Deleted -> deletedSnackbar(message.message.items)
                    else -> return
                }
            }
        }.show()
    }

    private fun CoordinatorLayout.deletedSnackbar(items: List<Node>): Snackbar {
        val message = items.takeIf { it.isNotEmpty() }?.let {
            val dirs = items.count { it.isDirectory }
            val files = items.size - dirs
            val what = listOfNotNull(
                resources.takeIf { dirs > 0 }?.getQuantityString(R.plurals.x_dirs, dirs, dirs),
                resources.takeIf { files > 0 }?.getQuantityString(R.plurals.x_files, files, files),
            ).joinToString(separator = resources.getString(R.string.and))
            resources.getQuantityString(R.plurals.x_deleted, items.size, what)
        } ?: resources.getString(R.string.nothing_was_deleted)
        return makeSnackbar(message, Snackbar.LENGTH_LONG)
    }

    private fun showPermissionRequiredWarning(unit: Unit) {
        binding.snackbarContainer.makeSnackbar(R.string.access_to_storage_forbidden, Snackbar.LENGTH_LONG)
            .setAction(R.string.allow) { presenter.onAllowStorageClick() }
            .show()
    }
}