package app.atomofiron.searchboxapp.screens.viewer

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.common.util.noClip
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.FragmentTextViewerBinding
import app.atomofiron.searchboxapp.custom.LayoutDelegate.apply
import app.atomofiron.searchboxapp.custom.LayoutDelegate.setScreenSizeListener
import app.atomofiron.searchboxapp.custom.overscroll.setupSpringOverscroll
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.model.ScreenSize
import app.atomofiron.searchboxapp.model.finder.LocalSearchTask
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator.ItemSeparatorDecorator
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.CharsetsHolder
import app.atomofiron.searchboxapp.screens.viewer.recycler.TextViewerAdapter
import app.atomofiron.searchboxapp.screens.viewer.state.MatchCursor
import app.atomofiron.searchboxapp.utils.addFastScroll
import app.atomofiron.searchboxapp.utils.showSnackbar
import app.atomofiron.searchboxapp.screens.viewer.state.TextViewerDockState.Companion.Default as DefaultDockState

class TextViewerFragment : Fragment(R.layout.fragment_text_viewer),
    BaseFragment<TextViewerFragment, TextViewerViewState, TextViewerPresenter, FragmentTextViewerBinding> by BaseFragmentImpl()
{
    private lateinit var binding: FragmentTextViewerBinding

    private val textAdapter = TextViewerAdapter()
    private var readingDenominator = 1
    private lateinit var charsetsHolder: CharsetsHolder

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
                setupSpringOverscroll()
                addOnScrollListener(OnScrollListenerImpl())
            }
            subBar.noClip()
            charsets.root.setHorizontalPadding(R.dimen.padding_semi)
            dockBar.submit(DefaultDockState)
            dockBar.setListener(::onBottomMenuItemClick)
            pathBar.setOnClickListener { presenter.onCopyPathClick() }
            toolbar.setNavigationOnClickListener { presenter.onNavigationClick() }
            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_more -> header.setExpanded(!header.isExpanded(), true)
                    R.id.menu_edit -> Unit
                    R.id.menu_save -> Unit
                }
                true
            }
            charsetsHolder = CharsetsHolder(presenter, charsets)
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
        viewCollect(alerts) { binding.snackbarContainer.showSnackbar(it) }
        viewCollect(reading) {
            binding.progress.isIndeterminate = it.length <= 0
            binding.progress.max = it.length
            binding.progress.secondaryProgress = it.loaded
            readingDenominator = it.denominator
        }
        viewCollect(charsets) { charsetsHolder.bind(it) }
    }

    override fun FragmentTextViewerBinding.onApplyInsets() {
        root.apply(recyclerView = recyclerView, dockView = dockBar, header = header, insetsBackground = insetsBackground)
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

    private fun onTaskChanged(task: LocalSearchTask?) {
        val matches = task?.result?.matches
        textAdapter.setMatches(matches)
        val iconId = if (task == null) R.drawable.ic_back else R.drawable.ic_cross
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), iconId)
    }

    private fun onMatchCursorChanged(cursor: MatchCursor?) = textAdapter.setCursor(cursor)

    private inner class OnScrollListenerImpl : RecyclerView.OnScrollListener() {

        private var view: View? = null

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val view = recyclerView.getChildAt(recyclerView.childCount.dec())
            if (view === this.view) {
                return
            }
            this.view = view
            if (recyclerView.childCount == viewState.textLines.value.size) {
                binding.progress.isVisible = false
                return
            }
            binding.progress.isVisible = true
            val holder = recyclerView.getChildViewHolder(view)
            val line = viewState.textLines.value
                .getOrNull(holder.bindingAdapterPosition)
                ?: return
            binding.progress.progress = (line.end / readingDenominator.toULong()).toInt()
        }
    }
}
