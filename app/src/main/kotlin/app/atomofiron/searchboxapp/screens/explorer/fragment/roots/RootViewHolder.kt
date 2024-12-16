package app.atomofiron.searchboxapp.screens.explorer.fragment.roots

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.MaterialAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.ItemExplorerCardBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.model.explorer.NodeRoot.NodeRootType
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.convert
import app.atomofiron.searchboxapp.utils.getColorByAttr

class RootViewHolder(itemView: View) : GeneralHolder<NodeRoot>(itemView) {
    companion object {
        fun Node.getTitle(resources: Resources): String = content.rootType?.getTitle(resources) ?: name

        fun NodeRootType.getTitle(resources: Resources): String? = when (this) {
            is NodeRootType.Photos -> resources.getString(R.string.root_photos)
            is NodeRootType.Videos -> resources.getString(R.string.root_videos)
            is NodeRootType.Camera -> resources.getString(R.string.root_camera)
            is NodeRootType.Screenshots -> resources.getString(R.string.root_screenshots)
            is NodeRootType.Downloads -> resources.getString(R.string.root_downloads)
            is NodeRootType.Bluetooth -> resources.getString(R.string.root_bluetooth)
            is NodeRootType.InternalStorage -> resources.getString(R.string.internal_storage)
            is NodeRootType.Favorite -> null
        }
    }

    private val suffixes = itemView.resources.getStringArray(R.array.size_suffix_arr)
    private val binding = ItemExplorerCardBinding.bind(itemView)
    private val colors = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf(0)),
        intArrayOf(
            context.getColorByAttr(MaterialAttr.colorPrimary),
            binding.cardTitle.textColors.defaultColor,
        )
    )

    init {
        binding.cardTitle.setTextColor(colors)
        binding.cardThumbnail.clipToOutline = true
    }

    override fun onBind(item: NodeRoot, position: Int) {
        val withArc = item.type is NodeRootType.InternalStorage
        binding.cardArc.isVisible = withArc
        binding.root.isSelected = item.isSelected
        binding.root.isEnabled = item.item.isCached
        binding.root.alpha = Alpha.enabled(item.item.isCached)
        binding.cardTitle.text = item.type.getTitle(itemView.resources)
        binding.cardThumbnail.imageTintList = if (item.withPreview) null else colors
        binding.cardThumbnail.background = item.getThumbnailBackground()
        binding.cardThumbnail.setImageDrawable((item.thumbnail ?: item.getIcon()))
        item.bindType()
    }

    private fun NodeRoot.bindType() {
        if (type !is NodeRootType.InternalStorage) return
        binding.cardArc.set(type.used, type.used + type.free)
        binding.cardArc.text = type.used.convert(suffixes, lossless = false, separator = "\u2009")
    }

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
            else -> ContextCompat.getDrawable(context, resId)
        }
    }
}