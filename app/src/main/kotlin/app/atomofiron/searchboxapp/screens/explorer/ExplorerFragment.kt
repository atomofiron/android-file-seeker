package app.atomofiron.searchboxapp.screens.explorer

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.core.content.ContextCompat
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
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerPagerAdapter
import app.atomofiron.searchboxapp.screens.explorer.state.ExplorerDock
import app.atomofiron.searchboxapp.screens.explorer.state.ExplorerDockState
import app.atomofiron.searchboxapp.screens.main.util.KeyCodeConsumer
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.getString
import app.atomofiron.searchboxapp.utils.makeSnackbar
import app.atomofiron.searchboxapp.utils.recyclerView
import com.google.android.material.snackbar.Snackbar
import lib.atomofiron.insets.insetsPadding

class ExplorerFragment : Fragment(R.layout.fragment_explorer),
    BaseFragment<ExplorerFragment, ExplorerViewState, ExplorerPresenter, FragmentExplorerBinding> by BaseFragmentImpl(),
    KeyCodeConsumer
{
    private lateinit var binding: FragmentExplorerBinding
    private lateinit var pagerAdapter: ExplorerPagerAdapter
    private val explorerViews get() = pagerAdapter.items
    private val tabIds = arrayOf(R.id.first_button, R.id.second_button)

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
        dockBar.submit(ExplorerDockState.Default)
        dockBar.setListener(::onNavigationItemSelected)
        pager.recyclerView.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                presenter.onTabSelected(position)
                explorerTabs.check(tabIds[position])
            }
        })

        explorerTabs.addOnButtonCheckedListener { group, id, isChecked ->
            if (!isChecked && group.checkedButtonId == View.NO_ID) {
                getCurrentTabView().scrollToTop()
                group.check(id)
            }
            if (isChecked) {
                pager.currentItem = tabIds.indexOf(id)
            }
        }
        explorerTabs.setOnClickListener {
            getCurrentTabView().scrollToTop()
        }

        val textColors = ContextCompat.getColorStateList(requireContext(), R.color.redio_text_button_foreground_color_selector)
        firstButton.setTextColor(textColors)
        secondButton.setTextColor(textColors)
    }

    private fun onNavigationItemSelected(item: DockItem) {
        when (item.id) {
            ExplorerDock.Search -> presenter.onSearchOptionSelected()
            ExplorerDock.Settings -> presenter.onSettingsOptionSelected()
        }
    }

    override fun ExplorerViewState.onViewCollect() {
        viewCollect(updates) {
            explorerViews.first().submit(it)
        }
        viewCollect(firstTabItems) {
            val first = explorerViews.first()
            first.submit(it)
            binding.firstButton.text = first.title ?: getString(R.string.dash)
        }
        /*viewCollect(secondTabItems) {
            val second = explorerViews.last()
            second.submitList(it)
            binding.secondButton.text = second.title ?: getString(R.string.dash)
        }*/
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
        binding.explorerTabs.insetsPadding(ExtType { barsWithCutout + dock }, start = true, top = true, end = true)
        binding.root.apply(dockView = binding.dockBar, /*tabLayout = explorerTabs,*/)
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
                    else -> return
                }
            }
        }.show()
    }

    private fun showPermissionRequiredWarning(unit: Unit) {
        binding.snackbarContainer.makeSnackbar(R.string.access_to_storage_forbidden, Snackbar.LENGTH_LONG)
            .setAction(R.string.allow) { presenter.onAllowStorageClick() }
            .show()
    }
}