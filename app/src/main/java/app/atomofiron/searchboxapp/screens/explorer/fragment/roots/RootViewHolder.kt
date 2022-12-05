package app.atomofiron.searchboxapp.screens.explorer.fragment.roots

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.ItemExplorerCardBinding
import app.atomofiron.searchboxapp.getColorByAttr
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.model.explorer.NodeRoot.NodeRootType
import app.atomofiron.searchboxapp.utils.Tool.convert

class RootViewHolder(
    itemView: View,
    private val rootAliases: Map<Int, String>,
) : GeneralHolder<NodeRoot>(itemView) {

    private val suffixes = itemView.resources.getStringArray(R.array.size_suffix_arr)
    private val binding = ItemExplorerCardBinding.bind(itemView)
    private val colors = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf(0)),
        intArrayOf(
            context.getColorByAttr(R.attr.colorPrimary),
            binding.cartTitle.textColors.defaultColor,
        )
    )

    init {
        binding.cartThumbnail.clipToOutline = true
        binding.cartTitle.setTextColor(colors)
    }

    override fun onBind(item: NodeRoot, position: Int) {
        val withArc = item.type is NodeRootType.InternalStorage
        binding.cartArc.isVisible = withArc
        binding.cartArcLabel.isVisible = withArc
        binding.root.isSelected = item.isSelected
        binding.cartTitle.text = item.getTitle()
        binding.cartThumbnail.imageTintList = if (item.withPreview) null else colors
        binding.cartThumbnail.background = item.getThumbnailBackground()
        binding.cartThumbnail.setImageDrawable((item.thumbnail ?: item.getIcon()))
        item.bindType()
    }

    private fun NodeRoot.bindType() {
        if (type !is NodeRootType.InternalStorage) return
        binding.cartArc.set(type.used, type.used + type.free)
        binding.cartArcLabel.text = type.used.convert(suffixes, lossless = false, separator = "\u2009")
    }

    private fun NodeRoot.getTitle(): String = rootAliases[item.uniqueId] ?: item.name

    private fun NodeRoot.getIcon(): Drawable {
        val resId = when (type) {
            is NodeRootType.Photos -> R.drawable.ic_thumbnail_camera
            is NodeRootType.Videos -> R.drawable.ic_thumbnail_videocam
            is NodeRootType.Camera -> R.drawable.ic_thumbnail_camera
            is NodeRootType.Downloads -> R.drawable.ic_thumbnail_download
            is NodeRootType.Bluetooth -> R.drawable.ic_thumbnail_bluetooth
            is NodeRootType.Screenshots -> R.drawable.ic_thumbnail_screenshot
            is NodeRootType.InternalStorage -> R.drawable.ic_thumbnail_memory
            is NodeRootType.Favorite -> R.drawable.ic_thumbnail_favorite
        }
        return ContextCompat.getDrawable(context, resId)!!
    }

    private fun NodeRoot.getThumbnailBackground(): Drawable? {
        val resId = when (type) {
            is NodeRootType.Photos,
            is NodeRootType.Videos,
            is NodeRootType.Camera,
            is NodeRootType.Screenshots -> R.drawable.item_root_thumbnail
            is NodeRootType.Downloads,
            is NodeRootType.Bluetooth,
            is NodeRootType.InternalStorage,
            is NodeRootType.Favorite -> 0
        }
        return when (resId) {
            0 -> null
            else ->  ContextCompat.getDrawable(context, resId)
        }
    }
}