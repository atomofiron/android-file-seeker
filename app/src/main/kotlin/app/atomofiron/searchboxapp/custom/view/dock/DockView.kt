package app.atomofiron.searchboxapp.custom.view.dock

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.graphics.Insets
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams.MATCH_PARENT
import androidx.recyclerview.widget.RecyclerView.LayoutParams.WRAP_CONTENT
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.noClip
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemChildren
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemColors
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemConfig
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemHolder
import app.atomofiron.searchboxapp.custom.view.dock.popup.DockPopupConfig
import app.atomofiron.searchboxapp.custom.view.dock.shape.DockBottomShape
import app.atomofiron.searchboxapp.custom.view.dock.shape.DockNotch
import app.atomofiron.searchboxapp.custom.view.dock.shape.DockStyle
import app.atomofiron.searchboxapp.model.Layout
import app.atomofiron.searchboxapp.utils.colorAttr
import app.atomofiron.searchboxapp.utils.removeOneIf

interface DockView {
    val canDrawShadow get() = Android.R
    val items: List<DockItem>
    fun setMode(mode: DockMode.Pinned)
    fun setStyle(style: DockStyle)
    fun submit(items: List<DockItem>)
    fun setListener(listener: (DockItem) -> Unit)
}

private val NotchStub = DockItem(DockItem.Id.Undefined, enabled = false)

@SuppressLint("ViewConstructor")
class DockViewImpl(
    context: Context,
    private var itemConfig: DockItemConfig = DockItemConfig.Stub,
    private var mode: DockMode? = null,
) : RecyclerView(context), DockView, Forwarding {

    private var listener: ((DockItem) -> Unit)? = null
    private val adapter = DockAdapter(itemConfig) { listener?.invoke(it) }
    private val gridManager = GridLayoutManager(context, 1)
    private val colors = DockItemColors(
        default = Color.TRANSPARENT,
        selected = context.colorAttr(MaterialAttr.colorSecondaryContainer),
        activated = context.colorAttr(MaterialAttr.colorPrimary),
        hovered = context.colorAttr(MaterialAttr.colorControlHighlight),
        focused = context.colorAttr(MaterialAttr.colorPrimary),
    )
    private val padding = resources.getDimensionPixelSize(R.dimen.dock_item_half_margin)
    private val contentPadding = resources.getDimensionPixelSize(R.dimen.content_margin)
    private val notchInset = resources.getDimension(R.dimen.dock_notch_inset)
    private var layoutDecorator = LayoutDecoration(adapter, itemConfig)
    private val shape = DockBottomShape(
        corners = resources.getDimension(R.dimen.dock_overlay_corner),
        style = DockStyle.Stub,
    )
    private val mutableItems = mutableListOf<DockItem>()
    private val itemCount get() = mutableItems.size
    override val items: List<DockItem> get() = mutableItems

    init {
        noClip()
        isClickable = true
        isFocusable = false
        itemAnimator = null
        layoutManager = gridManager
        overScrollMode = OVER_SCROLL_NEVER
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false
        background = shape
        elevation = resources.getDimension(R.dimen.dock_elevation)
        addItemDecoration(layoutDecorator)
        setPadding(padding, padding, padding, padding)
        super.setAdapter(adapter)
        if (isInEditMode) {
            adapter.submit(Array(5) { DockItem(DockItem.Id(it), DockItem.Icon(R.drawable.ic_circle_cross), DockItem.Label(R.string.done)) }.toList())
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            mode = DockMode.Pinned(Layout.Ground.Bottom, null)
        }
        mode?.apply()
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        super.setLayoutParams(params)
        val padding = padding * 2
        if (params != null && params.run { width > padding || height > padding } && itemConfig.isZero && !isInEditMode) {
            val width = if (params.width > padding) params.width else params.height
            val height = if (params.height > padding) params.height else params.width
            val new = itemConfig.copy(width = width - padding, height = height - padding)
            submit(config = new)
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        when (val mode = mode) {
            null ->  super.onMeasure(widthSpec, heightSpec)
            is DockMode.Popup -> super.onMeasure(
                MeasureSpec.makeMeasureSpec(itemConfig.popup.spaceWidth, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(itemConfig.popup.spaceHeight, MeasureSpec.AT_MOST),
            )
            is DockMode.Pinned -> {
                super.onMeasure(widthSpec, heightSpec)
                var width = MeasureSpec.getSize(widthSpec)
                var height = MeasureSpec.getSize(heightSpec)
                when {
                    mode.isBottom -> height -= measuredHeight
                    else -> width -= measuredWidth
                }
                submit(config = itemConfig.copy(popup = DockPopupConfig(mode.ground, width, height, animated = canDrawShadow, itemConfig.popup.style)))
            }
        }
        mode?.updateDecoration()
    }

    override fun setListener(listener: (DockItem) -> Unit) {
        this.listener = listener
    }

    override fun submit(items: List<DockItem>) {
        mutableItems.clear()
        mutableItems.addAll(items)
        adapter.submit(items)
        mode?.apply()
    }

    override fun setMode(mode: DockMode.Pinned) = submit(mode)

    override fun setStyle(style: DockStyle) {
        shape.setStyle(style)
        val colors = colors.copy(default = if (style.translucent) style.fill else Color.TRANSPARENT)
        val insets = when {
            style.translucent -> Insets.of(contentPadding - padding, padding, contentPadding - padding, padding)
            else -> Insets.of(padding, padding, padding, padding)
        }
        val new = itemConfig.copy(
            colors = colors,
            insets = insets,
            popup = itemConfig.popup.copy(style = style),
        )
        submit(config = new)
    }

    private fun submit(
        mode: DockMode? = this.mode,
        config: DockItemConfig = itemConfig,
    ) {
        when {
            mode != this.mode -> Unit
            config != itemConfig -> Unit
            else -> return
        }
        this.mode = mode
        itemConfig = config.with(ground = mode?.ground)
        adapter.set(itemConfig)
        mode?.apply()
    }

    private fun getNotch(): DockNotch? = (mode as? DockMode.Pinned)?.notch?.let {
        DockNotch(width = it.width + 2 * notchInset, height = it.height + notchInset)
    }

    private fun DockMode.apply() {
        val notch = getNotch()
        if ((notch == null) == mutableItems.any { it === NotchStub }) {
           mutableItems.run {
                when (notch) {
                    null -> removeOneIf { it === NotchStub }
                    else -> add(size / 2, NotchStub)
                }
                adapter.submit(mutableItems)
           }
        }
        shape.setNotch(notch)
        updateDecoration()
        gridManager.orientation = when (this) {
            is DockMode.Pinned -> if (isBottom) HORIZONTAL else VERTICAL
            is DockMode.Popup -> when {
                columns > DockItemChildren.AUTO -> VERTICAL
                isBottom -> HORIZONTAL
                else -> VERTICAL
            }
        }
        gridManager.spanCount = when (this) {
            is DockMode.Pinned -> 1
            is DockMode.Popup -> columns.coerceAtLeast(1)
        }
        (layoutParams as FrameLayout.LayoutParams?)?.run {
            gravity = when (ground.takeIf { this@apply is DockMode.Pinned }) {
                Layout.Ground.Left -> Gravity.LEFT
                Layout.Ground.Right -> Gravity.RIGHT
                Layout.Ground.Bottom -> Gravity.BOTTOM
                else -> gravity
            }
            when (this@apply) {
                is DockMode.Pinned -> when {
                    isBottom -> MATCH_PARENT to WRAP_CONTENT
                    else -> WRAP_CONTENT to MATCH_PARENT
                }
                is DockMode.Popup -> WRAP_CONTENT to WRAP_CONTENT
            }.let { (width, height) ->
                this.width = width
                this.height = height
            }
            super.setLayoutParams(this)
        }
    }

    private fun DockMode.updateDecoration() {
        val space = measuredWidth - paddingLeft - paddingRight
        val itemWidth = getNotch()
            ?.let { (space - it.width - padding * 2) / itemCount.dec() }
            ?.toInt()
        val config = when (this) {
            is DockMode.Pinned -> when {
                isBottom && itemCount > 1 -> when (itemWidth) {
                    null -> itemConfig.copy(width = space / itemCount)
                    else -> itemConfig.copy(width = itemWidth).also {
                        layoutDecorator.notch = space - itemWidth * itemCount.dec()
                    }
                }
                else -> itemConfig.copy(height = WRAP_CONTENT)
            }
            is DockMode.Popup -> itemConfig
        }
        layoutDecorator.set(config = config)
    }

    private class LayoutDecoration(
        private val adapter: DockAdapter,
        private var config: DockItemConfig,
    ) : ItemDecoration() {

        var notch = 0

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
            val holder = parent.getChildViewHolder(view)
            holder as DockItemHolder
            val item = adapter[holder.bindingAdapterPosition]
            view.updateLayoutParams {
                if (item === NotchStub) {
                    width = notch
                } else {
                    width = config.width
                    height = config.height
                }
            }
        }

        fun set(config: DockItemConfig = this.config) {
            if (config != this.config) {
                this.config = config
                adapter.notifyDataSetChanged()
            }
        }
    }
}
