package app.atomofiron.searchboxapp.screens.viewer

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.extension.debug
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.FragmentTextViewerBinding
import app.atomofiron.searchboxapp.custom.LayoutDelegate.apply
import app.atomofiron.searchboxapp.custom.LayoutDelegate.setScreenSizeListener
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.model.ScreenSize
import app.atomofiron.searchboxapp.model.finder.TextSearchTask
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator.ItemSeparatorDecorator
import app.atomofiron.searchboxapp.screens.viewer.recycler.TextViewerAdapter
import app.atomofiron.searchboxapp.screens.viewer.state.MatchCursor
import app.atomofiron.searchboxapp.utils.addFastScroll
import app.atomofiron.searchboxapp.screens.viewer.state.TextViewerDockState.Companion.Default as DefaultDockState

class TextViewerFragment : Fragment(R.layout.fragment_text_viewer),
    BaseFragment<TextViewerFragment, TextViewerViewState, TextViewerPresenter, FragmentTextViewerBinding> by BaseFragmentImpl()
{
    private lateinit var binding: FragmentTextViewerBinding

    private val textAdapter = TextViewerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, TextViewerViewModel::class, savedInstanceState)
        textAdapter.textViewerListener = presenter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentTextViewerBinding.bind(view).apply {
            recyclerView.addFastScroll(inTheEnd = true)
            recyclerView.run {
                adapter = textAdapter
                itemAnimator = null
                addItemDecoration(ItemSeparatorDecorator())
            }
            dockBar.submit(DefaultDockState)
            dockBar.setListener(::onBottomMenuItemClick)
            debug { toolbar.menu.add("Test") }
            pathBar.setOnClickListener { presenter.onCopyPathClick() }
            //copyPath.imageTintList = path.textColors
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
        viewCollect(item) {
            binding.toolbar.title = it.name
            binding.path.text = it.path
        }
        viewCollect(textLines, collector = textAdapter::submit)
        viewCollect(currentTask, collector = ::onTaskChanged)
        viewCollect(matchingCursor, collector = ::onMatchCursorChanged)
        viewCollect(dock, collector = binding.dockBar::submit)
    }

    override fun FragmentTextViewerBinding.onApplyInsets() {
        root.apply(recyclerView = recyclerView, dockView = dockBar, appBar = header)
    }

    private fun FragmentTextViewerBinding.configureAppBar() {
        root.setScreenSizeListener { _, height ->
            header.pinToolbar(height != ScreenSize.Compact)
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
        textAdapter.setMatches(matches)
        val iconId = if (task == null) R.drawable.ic_back else R.drawable.ic_cross
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), iconId)
    }

    private fun onMatchCursorChanged(cursor: MatchCursor?) = textAdapter.setCursor(cursor)
}
