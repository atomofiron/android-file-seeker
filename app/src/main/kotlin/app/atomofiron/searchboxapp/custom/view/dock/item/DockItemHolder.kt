package app.atomofiron.searchboxapp.custom.view.dock.item

import android.graphics.Rect
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.noClip
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemDockBinding
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem.Icon
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem.Label
import app.atomofiron.searchboxapp.custom.view.dock.popup.DockItemChildrenView
import app.atomofiron.searchboxapp.model.Layout.Ground
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class DockItemHolder(
    val binding: ItemDockBinding,
    private val selectListener: (DockItem) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var item: DockItem
    private var config = DockItemConfig.Stub

    init {
        binding.run {
            root.noClip()
            popup.noClip()
            root.setOnClickListener { onClick() }
        }
    }

    fun bind(item: DockItem, config: DockItemConfig) {
        if (config != this.config) {
            binding.popup.removeAllViews()
        }
        this.item = item
        this.config = config
        bind(item)
    }

    private fun bind(item: DockItem) = binding.run {
        when (val it = item.icon) {
            null -> icon.setImageDrawable(null)
            is Icon.Value -> icon.setImageDrawable(it.drawable)
            is Icon.Res -> icon.setImageResource(it.resId)
        }
        when (val it = item.label) {
            null -> label.text = null
            is Label.Value -> label.text = it.value
            is Label.Res -> label.setText(it.resId)
        }
        icon.isVisible = item.icon != null
        label.isVisible = item.label != null
        root.isSelected = item.selected
        root.isEnabled = item.enabled
        val emptyLabel = (item.label as? Label.Value)?.value?.isEmpty() == true
        icon.isEnabled = emptyLabel
        icon.isDuplicateParentStateEnabled = !emptyLabel
        if (item.children.isEmpty()) {
            popup.removeAllViews()
        }
    }

    private fun onClick() = when {
        item.children.isEmpty() -> selectListener(item)
        else -> binding.showPopup()
    }

    private fun ItemDockBinding.showPopup() {
        if (popup.isNotEmpty()) {
            return
        }
        val container = popup
        val corner = root.resources.getDimensionPixelSize(R.dimen.dock_overlay_corner)
        val offset = root.resources.getDimensionPixelSize(R.dimen.dock_item_half_margin)
        val parent = root.parent as RecyclerView
        val popup = config.popup
        val ground = popup.ground
        val minLeft = when (ground) {
            Ground.Bottom -> min(parent.paddingLeft, root.left) - offset - root.left
            Ground.Left -> root.width + offset
            Ground.Right -> -popup.spaceWidth - offset
        }
        val maxRight = when (ground) {
            Ground.Bottom -> parent.run { width - paddingRight + offset } - root.left
            Ground.Left, Ground.Right -> minLeft + popup.spaceWidth
        }
        var minTop = when (ground) {
            Ground.Bottom -> -popup.spaceHeight - offset
            Ground.Left, Ground.Right -> min(parent.paddingTop, root.top) - offset - root.top
        }
        var maxBottom = when (ground) {
            Ground.Bottom -> -offset
            Ground.Left, Ground.Right -> parent.run { height - paddingBottom + offset } - root.top
        }
        val bottomThreshold = root.height + offset
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
        val childConfig = config.copy(width = root.width, height = root.height, popup = popupConfig)
        val childrenView = DockItemChildrenView(root.context, item.children, childConfig, selectListener)
        container.addView(childrenView)
        childrenView.updateLayoutParams {
            width = childConfig.width
            height = childConfig.height
        }
        childrenView.expand()
    }
}
