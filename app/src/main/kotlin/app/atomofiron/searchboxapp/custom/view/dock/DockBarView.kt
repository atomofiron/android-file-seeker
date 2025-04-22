package app.atomofiron.searchboxapp.custom.view.dock

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams.MATCH_PARENT
import androidx.recyclerview.widget.RecyclerView.LayoutParams.WRAP_CONTENT
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.MaterialDimen
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.noClip
import app.atomofiron.fileseeker.R

class DockBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private var mode: DockMode? = null,
    private var itemConfig: DockItemConfig = DockItemConfig.Stub,
    items: List<DockItem> = emptyList(),
) : RecyclerView(context, attrs, defStyleAttr) {

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
        elevation = resources.getDimension(MaterialDimen.m3_comp_navigation_bar_container_elevation)
        setBackgroundColor(context.findColorByAttr(MaterialAttr.colorSurfaceContainer))
        setPaddingRelative(padding, padding, padding, padding)
        adapter.submitList(items)
        super.setAdapter(adapter)
        mode?.apply()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        parent.noClip()
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        super.setLayoutParams(params)
        if (params != null && params.width > 0 && params.height > 0 && itemConfig.isZero) {
            val padding = padding * 2
            val new = itemConfig.copy(width = params.width - padding, height = params.height - padding)
            if (new != itemConfig) {
                itemConfig = new
                mode?.apply()
            }
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        when (mode) {
            is DockMode.Popup -> super.onMeasure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED) // todo
            else -> super.onMeasure(widthSpec, heightSpec)
        }
    }

    fun setListener(listener: (DockItem) -> Unit) {
        this.listener = listener
    }

    fun submit(items: List<DockItem>) {
        adapter.submitList(items)
        mode?.apply()
    }

    fun setMode(mode: DockMode) {
        this.mode = mode
        itemConfig = itemConfig.copy(ground = mode.ground)
        mode.apply()
    }

    private fun DockMode.apply() {
        gridManager.orientation = when (this) {
            is DockMode.Pinned -> VERTICAL
            is DockMode.Popup -> when (true) {
                !itemConfig.isBottom,
                (columns > 1) -> VERTICAL
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
        layoutParams?.run {
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
