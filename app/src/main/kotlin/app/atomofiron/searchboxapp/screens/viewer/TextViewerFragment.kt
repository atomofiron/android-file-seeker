package app.atomofiron.searchboxapp.screens.viewer

import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.LayoutDelegate.setScreenSizeListener
import app.atomofiron.fileseeker.databinding.FragmentTextViewerBinding
import app.atomofiron.searchboxapp.custom.LayoutDelegate.apply
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.model.ScreenSize
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.finder.SearchTask
import app.atomofiron.searchboxapp.screens.viewer.recycler.TextViewerAdapter
import app.atomofiron.searchboxapp.screens.viewer.state.TextViewerDockState
import app.atomofiron.searchboxapp.screens.viewer.state.TextViewerDockState.Companion.Default as DefaultDockState
import app.atomofiron.searchboxapp.utils.addFastScroll
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL

class TextViewerFragment : Fragment(R.layout.fragment_text_viewer),
    BaseFragment<TextViewerFragment, TextViewerViewState, TextViewerPresenter, FragmentTextViewerBinding> by BaseFragmentImpl()
{
    companion object {
        const val KEY_PATH = "KEY_PATH"
        const val KEY_TASK_ID = "KEY_TASK_ID"
    }

    private lateinit var binding: FragmentTextViewerBinding

    private val viewerAdapter = TextViewerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, TextViewerViewModel::class, savedInstanceState)
        viewerAdapter.textViewerListener = presenter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentTextViewerBinding.bind(view).apply {
            recyclerView.addFastScroll(inTheEnd = true)
            recyclerView.run {
                layoutManager = LinearLayoutManager(context)
                adapter = viewerAdapter
                itemAnimator = null
            }
            (recyclerView.layoutParams as CoordinatorLayout.LayoutParams).run {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
            dockBar.submit(DefaultDockState)
            dockBar.setListener(::onBottomMenuItemClick)
            if (BuildConfig.DEBUG) toolbar.menu.add("Test")
            toolbar.setNavigationOnClickListener { presenter.onNavigationClick() }
            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_edit -> Unit
                    R.id.menu_save -> Unit
                }
                true
            }
            configureAppBar()
        }
        viewState.onViewCollect()
        binding.onApplyInsets()
    }

    override fun TextViewerViewState.onViewCollect() {
        viewCollect(item) { binding.toolbar.title = it.name }
        viewCollect(textLines, collector = viewerAdapter::submit)
        viewCollect(currentTask, collector = ::onTaskChanged)
        viewCollect(matchesCursor, collector = ::onMatchCursorChanged)
        viewCollect(dock, collector = binding.dockBar::submit)
    }

    override fun FragmentTextViewerBinding.onApplyInsets() {
        root.apply(recyclerView = recyclerView, dockView = dockBar, appBarLayout = appbarLayout)
    }

    private fun FragmentTextViewerBinding.configureAppBar() {
        root.setScreenSizeListener { _, height ->
            toolbar.updateLayoutParams<AppBarLayout.LayoutParams> {
                scrollFlags = if (height == ScreenSize.Compact) SCROLL_FLAG_SCROLL else SCROLL_FLAG_NO_SCROLL
            }
        }
        appbarLayout.addOnOffsetChangedListener { _, verticalOffset ->
            toolbar.alpha = (toolbar.height + verticalOffset) / toolbar.height.toFloat()
        }
    }

    private fun onBottomMenuItemClick(item: DockItem) {
        when (item.id) {
            DefaultDockState.status.id -> Unit
            DefaultDockState.search.id -> presenter.onSearchClick()
            DefaultDockState.previous.id -> presenter.onPreviousClick()
            DefaultDockState.next.id -> presenter.onNextClick()
        }
    }

    private fun onTaskChanged(task: SearchTask?) {
        val matches = (task?.result as SearchResult.TextSearchResult?)?.matchesMap
        viewerAdapter.setMatches(matches)
        val iconId = if (task == null) R.drawable.ic_back else R.drawable.ic_cross
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), iconId)
    }

    private fun onMatchCursorChanged(cursor: TextViewerViewState.MatchCursor) = viewerAdapter.setCursor(cursor)
}