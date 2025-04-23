package app.atomofiron.searchboxapp.custom.view.dock

import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.MaterialColor
import app.atomofiron.common.util.noClip
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemDockBinding
import app.atomofiron.searchboxapp.custom.view.dock.popup.DockItemChildrenView
import app.atomofiron.searchboxapp.model.Layout.Ground
import kotlin.math.abs
import kotlin.math.min

class DockItemHolder(
    private val binding: ItemDockBinding,
    private val selectListener: (DockItem) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var item: DockItem
    private var config = DockItemConfig.Stub

    init {
        binding.run {
            root.noClip()
            popup.noClip()
            root.setOnClickListener(::onClick)
            icon.imageTintList = ContextCompat.getColorStateList(root.context, MaterialColor.m3_navigation_item_icon_tint)
            label.setTextColor(ContextCompat.getColorStateList(root.context, MaterialColor.m3_navigation_item_text_color))
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
        when (item.icon) {
            0 -> icon.setImageDrawable(null)
            else -> icon.setImageResource(item.icon)
        }
        when (item.label) {
            0 -> label.text = null
            else -> label.setText(item.label)
        }
        label.isVisible = item.label != 0
        root.isSelected = item.selected
        root.isEnabled = item.enabled
        if (item.children.isEmpty()) {
            popup.removeAllViews()
        }
    }

    private fun onClick(view: View) = when {
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
        val minTop = when (ground) {
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
