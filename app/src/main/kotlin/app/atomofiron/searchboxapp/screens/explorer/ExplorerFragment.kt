package app.atomofiron.searchboxapp.screens.explorer

import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.ExplorerView
import app.atomofiron.searchboxapp.custom.LayoutDelegate
import app.atomofiron.searchboxapp.custom.drawable.NoticeableDrawable
import app.atomofiron.fileseeker.databinding.FragmentExplorerBinding
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerPagerAdapter
import app.atomofiron.searchboxapp.screens.main.util.KeyCodeConsumer
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.getString
import app.atomofiron.searchboxapp.utils.makeSnackbar
import app.atomofiron.searchboxapp.utils.recyclerView
import app.atomofiron.searchboxapp.utils.set
import com.google.android.material.snackbar.Snackbar
import lib.atomofiron.insets.insetsPadding

class ExplorerFragment : Fragment(R.layout.fragment_explorer),
    BaseFragment<ExplorerFragment, ExplorerViewState, ExplorerPresenter> by BaseFragmentImpl(),
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
        onApplyInsets(view)
    }

    private fun FragmentExplorerBinding.initView() {
        pager.adapter = pagerAdapter
        bottomBar.isItemActiveIndicatorEnabled = false
        bottomBar.setOnItemSelectedListener(::onNavigationItemSelected)
        bottomBar.menu.findItem(R.id.menu_settings).icon = NoticeableDrawable(requireContext(), R.drawable.ic_settings)
        navigationRail.menu.findItem(R.id.menu_settings).icon = NoticeableDrawable(requireContext(), R.drawable.ic_settings)
        binding.navigationRail.menu.removeItem(R.id.placeholder)
        navigationRail.setOnItemSelectedListener(::onNavigationItemSelected)
        navigationRail.isItemActiveIndicatorEnabled = false
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

    private fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_search -> presenter.onSearchOptionSelected()
            R.id.menu_settings -> presenter.onSettingsOptionSelected()
        }
        return false
    }

    override fun ExplorerViewState.onViewCollect() {
        //viewCollect(actions, collector = explorerAdapter::onAction)
        viewCollect(firstTabItems) {
            val first = explorerViews.first()
            first.submitList(it)
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
        viewCollect(alerts, collector = ::showAlert)
        viewCollect(settingsNotification, collector = ::setSettingsNotification)
        viewCollect(currentTab) {
            binding.pager.currentItem = it.index
        }
    }

    override fun onApplyInsets(root: View) {
        binding.run {
            explorerTabs.insetsPadding(ExtType { barsWithCutout + rail }, start = true, top = true, end = true)
            LayoutDelegate(
                this.root,
                bottomView = bottomBar,
                railView = navigationRail,
                //tabLayout = explorerTabs,
                joystickPlaceholder = bottomBar.menu.findItem(R.id.placeholder),
            )
        }
    }

    override fun onStart() {
        super.onStart()
        explorerViews.forEach { it.onItemsVisible() }
    }

    override fun onKeyDown(keyCode: Int): Boolean = when {
        !isVisible -> false
        keyCode != KeyEvent.KEYCODE_VOLUME_UP -> false
        else -> getCurrentTabView().isCurrentDirVisible()?.also {
            presenter.onVolumeUp(it)
        } != null
    }

    private fun getCurrentTabView(): ExplorerView = explorerViews[binding.pager.currentItem]

    private fun showAlert(error: NodeError) {
        binding.snackbarContainer.makeSnackbar(resources.getString(error), Snackbar.LENGTH_LONG).show()
    }

    private fun setSettingsNotification(value: Boolean) {
        binding.bottomBar.menu[R.id.menu_settings] = value
        binding.navigationRail.menu[R.id.menu_settings] = value
    }
}