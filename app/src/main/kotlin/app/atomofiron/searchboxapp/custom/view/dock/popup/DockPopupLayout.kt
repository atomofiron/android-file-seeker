package app.atomofiron.searchboxapp.custom.view.dock.popup

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemConfig
import app.atomofiron.searchboxapp.model.Layout.Ground
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val MAX_RATIO = 1.25f

class DockPopupLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    fun show(
        parent: RecyclerView,
        config: DockPopupConfig,
        itemView: View,
        item: DockItem,
        selectListener: (DockItem) -> Unit,
    ) {
        val container = this
        val corner = resources.getDimensionPixelSize(R.dimen.dock_overlay_corner)
        val offset = resources.getDimensionPixelSize(R.dimen.dock_item_half_margin)
        val ground = config.ground
        val minLeft = when (ground) {
            Ground.Bottom -> min(parent.paddingLeft, itemView.left) - offset - itemView.left
            Ground.Left -> itemView.width + offset
            Ground.Right -> -config.spaceWidth - offset
        }
        val maxRight = when (ground) {
            Ground.Bottom -> parent.run { width - paddingRight + offset } - itemView.left
            Ground.Left, Ground.Right -> minLeft + config.spaceWidth
        }
        var minTop = when (ground) {
            Ground.Bottom -> -config.spaceHeight - offset
            Ground.Left, Ground.Right -> min(parent.paddingTop, itemView.top) - offset - itemView.top
        }
        var maxBottom = when (ground) {
            Ground.Bottom -> -offset
            Ground.Left, Ground.Right -> parent.run { height - paddingBottom + offset } - itemView.top
        }
        val bottomThreshold = itemView.height + offset
        if (abs(maxBottom - bottomThreshold) < corner * 2) {
            maxBottom = bottomThreshold
        }
        maxBottom = max(maxBottom, bottomThreshold)
        val topThreshold = -offset
        if (abs(minTop - topThreshold) < corner * 2) {
            minTop = topThreshold
        }
        minTop = min(minTop, -topThreshold)
        val popupConfig = config.copy(rect = Rect(minLeft, minTop, maxRight, maxBottom))
        val itemWidth = min(itemView.width, (itemView.height * MAX_RATIO).toInt())
        val itemHeight = min(itemView.height, (itemView.width * MAX_RATIO).toInt())
        val childConfig = DockItemConfig(width = itemWidth, height =itemHeight, popup = popupConfig)
        val childrenView = DockItemChildrenView(itemView.context, item, childConfig, selectListener)
        container.addView(childrenView)
        childrenView.updateLayoutParams<LayoutParams> {
            width = childConfig.width
            height = childConfig.height
            gravity = Gravity.CENTER
        }
        childrenView.expand()
    }

    fun clear() = removeAllViews()
}
