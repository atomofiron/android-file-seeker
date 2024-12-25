package app.atomofiron.searchboxapp.custom

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import app.atomofiron.searchboxapp.databinding.ViewExplorerBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeTabItems
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerListDelegate
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerSpanSizeLookup
import app.atomofiron.searchboxapp.screens.explorer.fragment.SwipeMarkerDelegate
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerAdapter
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerItemActionListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.OnScrollIdleSubmitter
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootViewHolder.Companion.getTitle
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.disallowInterceptTouches
import app.atomofiron.searchboxapp.utils.scrollToTop
import lib.atomofiron.insets.attachInsetsListener
import lib.atomofiron.insets.insetsPadding

@SuppressLint("ViewConstructor")
class ExplorerView(
    context: Context,
    output: ExplorerViewOutput,
) : FrameLayout(context) {

    private val binding = ViewExplorerBinding.inflate(LayoutInflater.from(context), this)
    var title: String? = null
        private set

    private val rootAdapter = RootAdapter(output)
    private val explorerAdapter = ExplorerAdapter(output, ::onSeparatorClick)
    private val layoutManager = GridLayoutManager(context, 1)
    private val spanSizeLookup = ExplorerSpanSizeLookup(resources, layoutManager, rootAdapter)
    private val submitter = OnScrollIdleSubmitter(binding.recyclerView, explorerAdapter, explorerAdapter.visibleItems)
    private val swipeMarker = SwipeMarkerDelegate(resources)

    private val listDelegate: ExplorerListDelegate = ExplorerListDelegate(
        binding.recyclerView,
        rootAdapter,
        explorerAdapter,
        binding.explorerHeader,
        output,
    )

    init {
        binding.applyInsets()
        binding.init()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        spanSizeLookup.updateSpanCount(binding.recyclerView)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun ViewExplorerBinding.init() {
        layoutManager.spanSizeLookup = spanSizeLookup
        recyclerView.layoutManager = layoutManager
        val config = ConcatAdapter.Config.Builder()
            .setStableIdMode(ConcatAdapter.Config.StableIdMode.SHARED_STABLE_IDS)
            .build()
        recyclerView.adapter = ConcatAdapter(config, rootAdapter, explorerAdapter)
    }

    private fun ViewExplorerBinding.applyInsets() {
        recyclerView.insetsPadding(ExtType { barsWithCutout + navigation + rail })
        explorerHeader.insetsPadding(ExtType { barsWithCutout + rail }, start = true, top = true, end = true)
        root.attachInsetsListener(binding.systemUiBackground)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val result = when (event.action) {
            MotionEvent.ACTION_DOWN -> swipeMarker.onDown(binding.recyclerView, event.x, event.y)
                .also { if (it) parent.disallowInterceptTouches() }
            MotionEvent.ACTION_MOVE -> swipeMarker.onMove(binding.recyclerView, event.x, event.y)
            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> false.also { swipeMarker.onUp(binding.recyclerView, event.x, event.y) }
            else -> false
        }
        return result || super.dispatchTouchEvent(event)
    }

    fun scrollTo(item: Node) = listDelegate.scrollTo(item)

    fun scrollToTop() = binding.recyclerView.scrollToTop()

    fun isCurrentDirVisible(): Boolean? = listDelegate.isCurrentDirVisible()

    fun submitList(items: NodeTabItems) {
        rootAdapter.submitList(items.roots)
        submitter.submitListOnIdle(items.items, items.current?.path)
        listDelegate.setCurrentDir(items.current)
        title = items.current?.getTitle(resources)
    }

    fun setComposition(composition: ExplorerItemComposition) {
        listDelegate.setComposition(composition)
        explorerAdapter.setComposition(composition)
    }

    private fun onSeparatorClick(item: Node) = when {
        listDelegate.isVisible(item) -> listDelegate.highlight(item)
        else -> listDelegate.scrollTo(item)
    }

    interface ExplorerViewOutput : ExplorerItemActionListener, RootAdapter.RootClickListener
}