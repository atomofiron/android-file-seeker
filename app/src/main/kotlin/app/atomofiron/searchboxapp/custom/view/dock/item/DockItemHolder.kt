package app.atomofiron.searchboxapp.custom.view.dock.item

import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.noClip
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

    init {
        binding.run {
            root.noClip()
            popup.noClip()
            root.setOnClickListener { onClick() }
        }
    }

    fun bind(item: DockItem, config: DockPopupConfig?) {
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
        button.isEnabled = item.enabled
        root.isSelected = item.selected
        root.isClickable = item.clickable ?: item.enabled
        if (item.children.isEmpty()) {
            popup.clear()
        }
    }

    private fun ItemDockBinding.onClick() = when {
        item.children.isEmpty() -> selectListener(item)
        popup.isEmpty() -> popup.show(root.parent as RecyclerView, config!!, root, item, selectListener)
        else -> Unit
    }
}
