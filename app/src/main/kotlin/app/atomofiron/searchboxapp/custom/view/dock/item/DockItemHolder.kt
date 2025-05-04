package app.atomofiron.searchboxapp.custom.view.dock.item

import android.view.View
import androidx.core.graphics.Insets
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

class DockItemHolder(
    private val binding: ItemDockBinding,
    private val selectListener: (DockItem) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var item: DockItem
    private var config: DockItemConfig? = null
    private val drawable = DockItemDrawable(
        binding.root.context.findColorByAttr(MaterialAttr.colorControlHighlight),
        binding.root.resources.getDimension(R.dimen.dock_item_corner),
    )

    init {
        binding.run {
            root.noClip()
            popup.noClip()
            button.background = drawable
            button.setOnClickListener { onClick() }
        }
    }

    fun bind(item: DockItem, config: DockItemConfig?) {
        if (config != this.config) {
            binding.popup.clear()
        }
        this.item = item
        this.config = config
        bind(item)
        config ?: return
        drawable.setColors(config.colors)
        drawable.setInsets(config.insets)
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

    private fun ItemDockBinding.onClick() {
        if (item.children.isEmpty()) {
            selectListener(item)
        } else if (popup.isEmpty()) {
            var config = config!!
            config = config.copy(insets = Insets.of(config.insets.top, config.insets.top, config.insets.bottom, config.insets.bottom))
            binding.root.elevation = 1f
            val popupView = popup.show(root.parent as RecyclerView, config, root, item, selectListener)
            popupView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) = Unit
                override fun onViewDetachedFromWindow(v: View) {
                    binding.root.elevation = 0f
                }
            })
        }
    }
}
