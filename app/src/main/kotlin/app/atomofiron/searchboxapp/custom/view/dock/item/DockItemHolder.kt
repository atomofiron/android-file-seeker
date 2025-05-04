package app.atomofiron.searchboxapp.custom.view.dock.item

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.noClip
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemDockBinding
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem.Icon
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem.Label
import app.atomofiron.searchboxapp.custom.view.dock.popup.DockPopupConfig

class DockItemHolder(
    private val binding: ItemDockBinding,
    private val selectListener: (DockItem) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var item: DockItem
    private var config: DockPopupConfig? = null
    private val underlayColor = binding.root.context.findColorByAttr(android.R.attr.colorBackground)
    private val selectedColor = binding.root.context.findColorByAttr(MaterialAttr.colorSecondaryContainer)
    private val shapeDrawable: ShapeDrawable = binding.createRippleBackground()

    init {
        binding.run {
            root.noClip()
            popup.noClip()
            button.setOnClickListener { onClick() }
        }
    }

    fun bind(item: DockItem, config: DockPopupConfig?) {
        if (config != this.config) {
            binding.popup.removeAllViews()
        }
        this.item = item
        this.config = config
        bind(item)
        // todo define it in DockView
        updateBackground(transparent = config?.ground?.isBottom != false)
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
        button.isEnabled = item.enabled
        button.isSelected = item.selected
        button.isClickable = item.clickable ?: item.enabled
        if (item.children.isEmpty()) {
            popup.clear()
        }
    }

    private fun ItemDockBinding.createRippleBackground(): ShapeDrawable {
        val corners = root.resources.getDimension(R.dimen.dock_item_corner)
        val ripple = root.context.findColorByAttr(MaterialAttr.colorControlHighlight)
        val shape = RoundRectShape(FloatArray(8) { corners }, null, null)
        val shapeDrawable = ShapeDrawable(shape)
        val mask = ShapeDrawable(shape)
        val background = RippleDrawable(ColorStateList.valueOf(ripple), shapeDrawable, mask)
        button.background = background
        return shapeDrawable
    }

    private fun updateBackground(transparent: Boolean?) {
        val defaultColor = if (transparent == false) underlayColor else Color.TRANSPARENT
        val colors = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf(0)),
            intArrayOf(selectedColor, defaultColor),
        )
        shapeDrawable.setTintList(colors)
    }

    private fun ItemDockBinding.onClick() = when {
        item.children.isEmpty() -> selectListener(item)
        popup.isEmpty() -> popup.show(root.parent as RecyclerView, config!!, root, item, selectListener)
        else -> Unit
    }
}
