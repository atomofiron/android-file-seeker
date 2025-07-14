package app.atomofiron.searchboxapp.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.databinding.ViewExplorerBinding
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
import app.atomofiron.searchboxapp.utils.addFastScroll
import app.atomofiron.searchboxapp.utils.disallowInterceptTouches
import app.atomofiron.searchboxapp.utils.scrollToTop
import lib.atomofiron.insets.attachInsetsListener
import lib.atomofiron.insets.insetsPadding

@SuppressLint("ViewConstructor")
class ExplorerView(
    context: Context,
    private val output: ExplorerViewOutput,
) : FrameLayout(context) {

    private val binding = ViewExplorerBinding.inflate(LayoutInflater.from(context), this)
    var title: String? = null
        private set

    private val rootAdapter = RootAdapter(output)
    private val explorerAdapter = ExplorerAdapter(output, ::onSeparatorClick)
    private val layoutManager = GridLayoutManager(context, 1)
    private val spanSizeLookup = ExplorerSpanSizeLookup(resources, layoutManager, rootAdapter)
    private val submitter = OnScrollIdleSubmitter(binding.recyclerView, explorerAdapter)
    private val swipeMarker = SwipeMarkerDelegate(resources)

    private val listDelegate: ExplorerListDelegate = ExplorerListDelegate(
        binding.recyclerView,
        rootAdapter,
        explorerAdapter,
        binding.explorerHeader,
        output,
    )

    init {
        binding.recyclerView.addExplorerFastScroll()
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
        recyclerView.insetsPadding(ExtType { barsWithCutout + dock })
        explorerHeader.insetsPadding(ExtType { barsWithCutout + dock }, start = true, top = true, end = true)
        root.attachInsetsListener(binding.systemUiBackground)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return swipeMarker.onTouch(binding.recyclerView, event) || super.dispatchTouchEvent(event)
    }

    fun scrollTo(item: Node) = listDelegate.scrollTo(item)

    fun scrollToTop(): Boolean = binding.recyclerView.scrollToTop()

    fun isCurrentDirVisible(): Boolean? = listDelegate.isDeepestDirVisible()

    fun submit(items: NodeTabItems) {
        rootAdapter.submitList(items.roots)
        submitter.submitOnIdle(items.items, marker = items.deepest?.path)
        listDelegate.set(items.items)
        title = items.deepest?.getTitle(resources)
    }

    fun submit(item: Node) = submitter.submit(item)

    fun setComposition(composition: ExplorerItemComposition) {
        listDelegate.setComposition(composition)
        explorerAdapter.setComposition(composition)
    }

    fun onItemsVisible() {
        val items = explorerAdapter.visibleItems.mapNotNull { explorerAdapter.items.getOrNull(it) }
        output.onItemsBecomeVisible(items)
    }

    private fun onSeparatorClick(item: Node) = when {
        listDelegate.isVisible(item) -> listDelegate.highlight(item)
        else -> listDelegate.scrollTo(item)
    }

    interface ExplorerViewOutput : ExplorerItemActionListener, RootAdapter.RootClickListener
}

fun RecyclerView.addExplorerFastScroll() {
    val container = parent as View
    val scroller = addFastScroll(
        inTheEnd = false,
        callback = {
            when (it) {
                FastScroller2.Action.Redraw -> container.foreground?.invalidateSelf()
                FastScroller2.Action.DragStart -> parent.disallowInterceptTouches()
            }
        },
    )
    // вся это нужно для того,
    // чтобы скролл рисовался поверх пинящегося заголовка,
    // который лежит с ресайклером в одном контейнере
    removeItemDecoration(scroller)
    val foreground = object : Drawable() {
        val stub = RecyclerView.State()
        override fun draw(canvas: Canvas) = scroller.onDrawOver(canvas, this@addExplorerFastScroll, stub)
        override fun setAlpha(alpha: Int) = Unit
        override fun setColorFilter(colorFilter: ColorFilter?) = Unit
        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }
    container.foreground = foreground
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) = foreground.invalidateSelf()
    })
}
