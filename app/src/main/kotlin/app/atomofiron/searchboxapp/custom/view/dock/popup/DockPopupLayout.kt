package app.atomofiron.searchboxapp.custom.view.dock.popup

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemChildren
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemConfig
import app.atomofiron.searchboxapp.model.Layout.Ground
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class DockPopupLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    fun show(
        parent: RecyclerView,
        config: DockItemConfig,
        itemView: View,
        children: DockItemChildren,
        selectListener: (DockItem) -> Unit,
    ) {
        val container = this
        val corner = resources.getDimensionPixelSize(R.dimen.dock_overlay_corner)
        val offset = resources.getDimensionPixelSize(R.dimen.dock_item_half_margin)
        val popup = config.popup
        val ground = popup.ground
        val minLeft = when (ground) {
            Ground.Bottom -> min(parent.paddingLeft, itemView.left) - offset - itemView.left
            Ground.Left -> itemView.width + offset
            Ground.Right -> -popup.spaceWidth - offset
        }
        val maxRight = when (ground) {
            Ground.Bottom -> parent.run { width - paddingRight + offset } - itemView.left
            Ground.Left, Ground.Right -> minLeft + popup.spaceWidth
        }
        var minTop = when (ground) {
            Ground.Bottom -> -popup.spaceHeight - offset
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
        val popupConfig = popup.copy(rect = Rect(minLeft, minTop, maxRight, maxBottom))
        val childConfig = config.copy(width = itemView.width, height = itemView.height, popup = popupConfig)
        val childrenView = DockItemChildrenView(itemView.context, children, childConfig, selectListener)
        container.addView(childrenView)
        childrenView.updateLayoutParams {
            width = childConfig.width
            height = childConfig.height
        }
        childrenView.expand()
    }

    fun clear() = removeAllViews()
}
