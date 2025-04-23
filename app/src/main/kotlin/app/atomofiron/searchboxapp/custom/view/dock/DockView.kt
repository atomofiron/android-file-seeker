package app.atomofiron.searchboxapp.custom.view.dock

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams.MATCH_PARENT
import androidx.recyclerview.widget.RecyclerView.LayoutParams.WRAP_CONTENT
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.noClip
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.popup.DockPopupConfig
import app.atomofiron.searchboxapp.model.Layout

interface DockView {
    fun setMode(mode: DockMode)
    fun submit(items: List<DockItem>)
    fun setListener(listener: (DockItem) -> Unit)
}

@SuppressLint("ViewConstructor")
class DockViewImpl(
    context: Context,
    items: List<DockItem> = emptyList(),
    private var itemConfig: DockItemConfig = DockItemConfig.Stub,
    private var mode: DockMode? = null,
) : RecyclerView(context), DockView, Forwarding {

    private var listener: ((DockItem) -> Unit)? = null
    private val adapter = DockAdapter { listener?.invoke(it) }
    private val gridManager = GridLayoutManager(context, 322)
    private val padding = resources.getDimensionPixelSize(R.dimen.dock_item_half_margin)

    init {
        noClip()
        isClickable = true
        isFocusable = false
        itemAnimator = null
        layoutManager = gridManager
        overScrollMode = OVER_SCROLL_NEVER
        setBackgroundColor(context.findColorByAttr(MaterialAttr.colorSurfaceContainer))
        setPadding(padding, padding, padding, padding)
        adapter.submitList(items)
        super.setAdapter(adapter)
        if (isInEditMode) {
            adapter.itemConfig = DockItemConfig.Stub.copy(width = MATCH_PARENT, height = WRAP_CONTENT)
            adapter.submitList(Array(5) { DockItem(R.drawable.ic_circle_cross, R.string.done) }.toList())
            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            mode = DockMode.Pinned(Layout.Ground.Bottom)
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
                submit(config = itemConfig.copy(popup = DockPopupConfig(mode.ground, width, height)))
            }
        }
    }

    override fun setListener(listener: (DockItem) -> Unit) {
        this.listener = listener
    }

    override fun submit(items: List<DockItem>) {
        adapter.submitList(items)
        mode?.apply()
    }

    override fun setMode(mode: DockMode) = submit(mode)

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
        mode?.apply()
    }

    private fun DockMode.apply() {
        gridManager.orientation = when (this) {
            is DockMode.Pinned -> VERTICAL
            is DockMode.Popup -> when (true) {
                (columns > DockItemChildren.AUTO),
                !isBottom -> VERTICAL
                else -> HORIZONTAL
            }
        }
        gridManager.spanCount = when (this) {
            is DockMode.Pinned -> if (isBottom) adapter.itemCount else 1
            is DockMode.Popup -> columns
        }.coerceAtLeast(1)
        adapter.itemConfig = when (this) {
            is DockMode.Pinned -> when {
                isBottom -> itemConfig.copy(width = MATCH_PARENT)
                else -> itemConfig.copy(height = WRAP_CONTENT)
            }
            is DockMode.Popup -> itemConfig
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
}
