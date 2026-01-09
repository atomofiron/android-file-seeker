package app.atomofiron.searchboxapp.screens.explorer.fragment.roots

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemExplorerCardBinding
import app.atomofiron.searchboxapp.custom.drawable.colorSurfaceContainer
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.model.explorer.NodeRootInfo
import app.atomofiron.searchboxapp.model.explorer.NodeStorage
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.colorAttr
import app.atomofiron.searchboxapp.utils.convert
import app.atomofiron.searchboxapp.utils.drawable
import com.bumptech.glide.Glide

class RootViewHolder(itemView: View) : GeneralHolder<NodeRoot>(itemView) {
    companion object {

        fun Node.getTitle(resources: Resources): String = content.rootType?.getTitle(resources) ?: name

        fun NodeRootInfo.getTitle(resources: Resources): String? = when (this) {
            is NodeRootInfo.Photos -> resources.getString(R.string.root_photos)
            is NodeRootInfo.Videos -> resources.getString(R.string.root_videos)
            is NodeRootInfo.Camera -> resources.getString(R.string.root_camera)
            is NodeRootInfo.Screenshots -> resources.getString(R.string.root_screenshots)
            is NodeRootInfo.Downloads -> resources.getString(R.string.root_downloads)
            is NodeRootInfo.Bluetooth -> resources.getString(R.string.root_bluetooth)
            is NodeRootInfo.Storage -> when (kind) {
                NodeStorage.Kind.InternalStorage -> resources.getString(R.string.internal_storage)
                NodeStorage.Kind.SdCard -> resources.getString(R.string.sdcard)
                NodeStorage.Kind.UsbStorage -> info.alias ?: info.name ?: resources.getString(R.string.usb_storage)
            }
            is NodeRootInfo.Favorite -> null
            is NodeRootInfo.SystemRoot -> resources.getString(R.string.system_root)
        }
    }

    private val suffixes = itemView.resources.getStringArray(R.array.size_suffix_arr)
    private val binding = ItemExplorerCardBinding.bind(itemView)
    private val colors = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf(0)),
        intArrayOf(
            context.colorAttr(MaterialAttr.colorPrimary),
            binding.cardTitle.textColors.defaultColor,
        )
    )

    init {
        binding.cardTitle.setTextColor(colors)
        binding.cardThumbnail.clipToOutline = true
        binding.root.setCardBackgroundColor(context.colorSurfaceContainer())
    }

    override fun onBind(item: NodeRoot, position: Int) = binding.run {
        val withArc = item.info is NodeRootInfo.Storage
        cardArc.isVisible = withArc
        root.isSelected = item.isSelected
        root.isEnabled = item.isEnabled
        root.alpha = Alpha.enabled(item.isEnabled)
        cardTitle.text = item.info.getTitle(itemView.resources)
        cardThumbnail.background = item.getThumbnailBackground()
        when (val thumbnail = item.thumbnail) {
            is Thumbnail.FilePath -> Glide
                .with(root.context)
                .load(item.thumbnailPath)
                .placeholder(item.getIcon().tinted())
                .into(cardThumbnail)
            null, Thumbnail.Loading -> cardThumbnail.setImageDrawable(item.getIcon().tinted())
            is Thumbnail.Bitmap -> cardThumbnail.setImageBitmap(thumbnail.value)
            is Thumbnail.Drawable -> cardThumbnail.setImageDrawable(thumbnail.value)
            is Thumbnail.Res -> cardThumbnail.setImageDrawable(context.drawable(thumbnail.value).tinted())
        }
        item.bindType()
    }

    private fun NodeRoot.bindType() {
        if (info is NodeRootInfo.Storage) {
            binding.cardArc.set(progress = info.used, max = info.total)
            binding.cardArc.text = info.used.convert(suffixes, lossless = false, separator = "\u2009")
        }
    }

    private fun NodeRoot.getIcon(): Drawable {
        val resId = when (info) {
            is NodeRootInfo.Photos -> R.drawable.ic_thumbnail_camera
            is NodeRootInfo.Videos -> R.drawable.ic_thumbnail_videocam
            is NodeRootInfo.Camera -> R.drawable.ic_thumbnail_camera
            is NodeRootInfo.Downloads -> R.drawable.ic_thumbnail_download
            is NodeRootInfo.Bluetooth -> R.drawable.ic_thumbnail_bluetooth
            is NodeRootInfo.Screenshots -> R.drawable.ic_thumbnail_screenshot
            is NodeRootInfo.Storage -> when (info.kind) {
                NodeStorage.Kind.InternalStorage -> R.drawable.ic_thumbnail_memory
                NodeStorage.Kind.SdCard -> R.drawable.ic_thumbnail_micro_sd
                NodeStorage.Kind.UsbStorage -> R.drawable.ic_thumbnail_usb_flash
            }
            is NodeRootInfo.Favorite -> R.drawable.ic_thumbnail_favorite
            is NodeRootInfo.SystemRoot -> R.drawable.ic_thumbnail_system
        }
        return ContextCompat.getDrawable(context, resId)?.mutate() as Drawable
    }

    private fun NodeRoot.getThumbnailBackground(): Drawable? = when (info) {
        is NodeRootInfo.Storage -> null
        is NodeRootInfo.Favorite,
        is NodeRootInfo.Photos,
        is NodeRootInfo.Videos,
        is NodeRootInfo.Camera,
        is NodeRootInfo.Screenshots,
        is NodeRootInfo.Downloads,
        is NodeRootInfo.SystemRoot,
        is NodeRootInfo.Bluetooth -> ContextCompat.getDrawable(context, R.drawable.item_root_thumbnail)
    }

    private fun Drawable.tinted(): Drawable {
        setTintList(colors)
        return this
    }
}