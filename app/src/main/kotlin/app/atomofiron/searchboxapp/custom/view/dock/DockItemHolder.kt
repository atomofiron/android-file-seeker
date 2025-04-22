package app.atomofiron.searchboxapp.custom.view.dock

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.MaterialColor
import app.atomofiron.common.util.noClip
import app.atomofiron.fileseeker.databinding.ItemDockBinding
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemChildrenView

class DockItemHolder(
    private val binding: ItemDockBinding,
    private val selectListener: (DockItem) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var item: DockItem

    init {
        binding.run {
            root.noClip()
            overlay.noClip()
            root.setOnClickListener(::onClick)
            icon.imageTintList = ContextCompat.getColorStateList(root.context, MaterialColor.m3_navigation_item_icon_tint)
            label.setTextColor(ContextCompat.getColorStateList(root.context, MaterialColor.m3_navigation_item_text_color))
        }
    }

    fun bind(item: DockItem) = binding.run {
        this@DockItemHolder.item = item
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
            overlay.removeAllViews()
        }
    }

    private fun onClick(view: View) = when {
        item.children.isEmpty() -> selectListener(item)
        else -> binding.expand()
    }

    private fun ItemDockBinding.expand() {
        if (overlay.isNotEmpty()) {
            return
        }
        val childrenView = DockItemChildrenView(root.context, item.children, root.width, root.height, selectListener)
        overlay.addView(childrenView)
        childrenView.updateLayoutParams {
            width = root.width
            height = root.height
        }
        childrenView.expand()
    }
}
