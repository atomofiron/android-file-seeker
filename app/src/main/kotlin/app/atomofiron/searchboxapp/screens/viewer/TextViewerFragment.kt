package app.atomofiron.searchboxapp.screens.viewer

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.extension.debug
import app.atomofiron.common.util.extension.debugRequire
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.FragmentTextViewerBinding
import app.atomofiron.searchboxapp.custom.LayoutDelegate.apply
import app.atomofiron.searchboxapp.custom.LayoutDelegate.setScreenSizeListener
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.model.ScreenSize
import app.atomofiron.searchboxapp.model.finder.TextSearchTask
import app.atomofiron.searchboxapp.screens.viewer.recycler.TextViewerAdapter
import app.atomofiron.searchboxapp.utils.addFastScroll
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import com.google.android.material.appbar.MaterialToolbar
import app.atomofiron.searchboxapp.screens.viewer.state.TextViewerDockState.Companion.Default as DefaultDockState

class TextViewerFragment : Fragment(R.layout.fragment_text_viewer),
    BaseFragment<TextViewerFragment, TextViewerViewState, TextViewerPresenter, FragmentTextViewerBinding> by BaseFragmentImpl()
{
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
            debug { toolbar.menu.add("Test") }
            toolbar.setNavigationOnClickListener { presenter.onNavigationClick() }
            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_edit -> Unit
                    R.id.menu_save -> Unit
                }
                true
            }
            toolbar.fixSubtitle()
            configureAppBar()
        }
        viewState.onViewCollect()
        binding.onApplyInsets()
    }

    private fun MaterialToolbar.fixSubtitle() {
        val were = children.toList()
        subtitle = "…"
        val now = children.toList()
        val target = now.find { child ->
            were.none { it === child }
        } as TextView?
        target?.ellipsize = TextUtils.TruncateAt.MIDDLE
        debugRequire(target != null) { "subtitle is null" }
    }

    override fun TextViewerViewState.onViewCollect() {
        viewCollect(item) {
            binding.toolbar.title = it.name
            binding.toolbar.subtitle = it.path
        }
        viewCollect(textLines, collector = viewerAdapter::submit)
        viewCollect(currentTask, collector = ::onTaskChanged)
        viewCollect(matchesCursor, collector = ::onMatchCursorChanged)
        viewCollect(dock, collector = binding.dockBar::submit)
    }

    override fun FragmentTextViewerBinding.onApplyInsets() {
        root.apply(recyclerView = recyclerView, dockView = dockBar, appBarLayout = appbar)
    }

    private fun FragmentTextViewerBinding.configureAppBar() {
        root.setScreenSizeListener { _, height ->
            val collapsable = height == ScreenSize.Compact
            appbar.isLiftOnScroll = !collapsable
            toolbar.updateLayoutParams<AppBarLayout.LayoutParams> {
                scrollFlags = if (collapsable) SCROLL_FLAG_SCROLL else SCROLL_FLAG_NO_SCROLL
            }
        }
        appbar.addOnOffsetChangedListener { _, verticalOffset ->
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

    private fun onTaskChanged(task: TextSearchTask?) {
        val matches = task?.result?.matches
        viewerAdapter.setMatches(matches)
        val iconId = if (task == null) R.drawable.ic_back else R.drawable.ic_cross
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), iconId)
    }

    private fun onMatchCursorChanged(cursor: TextViewerViewState.MatchCursor?) = viewerAdapter.setCursor(cursor)
}