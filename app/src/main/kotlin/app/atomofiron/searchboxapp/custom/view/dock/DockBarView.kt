package app.atomofiron.searchboxapp.custom.view.dock

import android.content.Context
import android.util.AttributeSet
import android.util.Size
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
import app.atomofiron.searchboxapp.model.Layout

private val Zero = Size(0, 0)

class DockBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private var mode: DockMode? = null,
) : RecyclerView(context, attrs, defStyleAttr) {

    private var listener: ((DockItem) -> Unit)? = null
    private val adapter = DockAdapter { listener?.invoke(it) }
    private val gridManager = GridLayoutManager(context, 1, GridLayoutManager.VERTICAL, false)
    private var itemSize = Zero
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
        super.setAdapter(adapter)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        parent.noClip()
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        super.setLayoutParams(params)
        if (params != null && params.width > 0 && params.height > 0 && itemSize == Zero) {
            val padding = padding * 2
            itemSize = Size(params.width - padding, params.height - padding)
            mode?.apply()
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        when (mode) {
            is DockMode.Children -> super.onMeasure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
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

    fun setMode(bottom: Layout) {
        mode = if (bottom.isBottom) DockMode.Bottom else DockMode.Flank
        mode?.apply()
    }

    private fun DockMode.apply() {
        gridManager.spanCount = when (this) {
            is DockMode.Bottom -> adapter.itemCount
            is DockMode.Flank -> 1
            is DockMode.Children -> columns
        }
        adapter.itemSize = when (this) {
            is DockMode.Bottom -> Size(MATCH_PARENT, itemSize.height)
            is DockMode.Flank -> Size(itemSize.width, WRAP_CONTENT)
            is DockMode.Children -> Size(width, height)
        }
        layoutParams?.run {
            when (this@apply) {
                is DockMode.Bottom -> MATCH_PARENT to WRAP_CONTENT
                is DockMode.Flank -> WRAP_CONTENT to MATCH_PARENT
                is DockMode.Children -> WRAP_CONTENT to WRAP_CONTENT
            }.let { (width, height) ->
                this.width = width
                this.height = height
            }
            super.setLayoutParams(this)
        }
    }
}
