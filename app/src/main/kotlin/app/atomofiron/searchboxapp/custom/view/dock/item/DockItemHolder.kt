package app.atomofiron.searchboxapp.custom.view.dock.item

import android.content.res.ColorStateList
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.noClip
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemDockBinding
import app.atomofiron.searchboxapp.custom.drawable.MuonsDrawable.Companion.setMuonsDrawable
import app.atomofiron.searchboxapp.custom.drawable.NoticeableDrawable
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem.Icon
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem.Label
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.colorAttr
import app.atomofiron.searchboxapp.utils.performHapticLite

class DockItemHolder(
    private val binding: ItemDockBinding,
    private val selectListener: (DockItem) -> Unit,
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var item: DockItem
    private var config: DockItemConfig? = null
    private val drawable = DockItemDrawable(
        ripple = binding.root.context.findColorByAttr(MaterialAttr.colorControlHighlight),
        primary = binding.root.context.findColorByAttr(MaterialAttr.colorPrimary),
        surface = binding.root.context.findColorByAttr(MaterialAttr.colorSurface),
        stroke = binding.root.resources.getDimension(R.dimen.stroke_width),
        corners = binding.root.resources.getDimension(R.dimen.dock_item_corner),
    )

    init {
        binding.run {
            root.noClip()
            popup.noClip()
            button.background = drawable
            button.setOnClickListener { onClick() }
            ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_activated), intArrayOf()),
                intArrayOf(
                    root.context.colorAttr(MaterialAttr.colorOnPrimary),
                    root.context.colorAttr(MaterialAttr.colorOnSurface),
                ),
            ).let {
                icon.imageTintList = it
                label.setTextColor(it)
            }
        }
    }

    fun bind(item: DockItem, config: DockItemConfig?) {
        if (item.children.isEmpty() || config != this.config) {
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
        if (item.progress) {
            icon.setMuonsDrawable()
        }
        if (item.notice) icon.drawable
            .let { it ?: ContextCompat.getDrawable(icon.context, R.drawable.ic_dock_empty)!! }
            .let { NoticeableDrawable(icon.context, it).forceShowDot(true) }
            .let { icon.setImageDrawable(it) }
        when (val it = item.label) {
            null -> label.text = null
            is Label.Value -> label.text = it.value
            is Label.Res -> label.setText(it.resId)
        }
        icon.isVisible = icon.drawable != null
        label.isVisible = item.label != null
        button.isEnabled = item.enabled
        button.isSelected = item.selected
        button.isActivated = item.primary
        button.isClickable = item.clickable ?: item.enabled
        button.alpha = Alpha.enabled(!item.primary || item.enabled)
        popup.bind(item)
    }

    private fun ItemDockBinding.onClick() {
        if (item.children.isEmpty()) {
            selectListener(item)
        } else if (popup.isNotEmpty()) {
            popup.collapse()
        } else {
            var config = config!!
            config = config.copy(insets = Insets.of(config.insets.top, config.insets.top, config.insets.bottom, config.insets.bottom))
            root.elevation = 1f
            val popupView = popup.show(root.parent as RecyclerView, config, root, item, selectListener)
            popupView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) = Unit
                override fun onViewDetachedFromWindow(v: View) {
                    root.elevation = 0f
                    icon.drawable?.callback = icon
                }
            })
        }
        root.performHapticLite()
    }
}
