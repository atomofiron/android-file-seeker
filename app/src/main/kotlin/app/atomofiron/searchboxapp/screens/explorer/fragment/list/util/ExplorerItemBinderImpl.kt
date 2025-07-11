package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.text.style.ImageSpan.ALIGN_BASELINE
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import app.atomofiron.common.util.ifVisible
import com.google.android.material.checkbox.MaterialCheckBox
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.LemonDrawable
import app.atomofiron.searchboxapp.custom.drawable.MuonsDrawable
import app.atomofiron.searchboxapp.custom.drawable.translated
import app.atomofiron.searchboxapp.custom.view.MuonsView
import app.atomofiron.searchboxapp.model.explorer.*
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerItemActionListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootViewHolder.Companion.getTitle
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.getString
import app.atomofiron.searchboxapp.utils.isRtl
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView

class ExplorerItemBinderImpl(
    private val itemView: View,
) : ExplorerItemBinder {
    companion object {
        private const val SPACE = " "
        private const val EMPTY = ""
    }

    private lateinit var item: Node

    private var dirIcon = ContextCompat.getDrawable(itemView.context, R.drawable.ic_explorer_folder)!!.mutate().translated()
    private var fileIcon = ContextCompat.getDrawable(itemView.context, R.drawable.ic_explorer_file)!!.mutate().translated()
    private val placeholder = MuonsDrawable(itemView.context)

    private val ivIcon = itemView.findViewById<ImageView>(R.id.item_explorer_iv_icon)
    private val ivThumbnail = itemView.findViewById<ImageView>(R.id.item_explorer_iv_thumbnail)
    private val tvName = itemView.findViewById<TextView>(R.id.item_explorer_tv_title)
    private val tvDescription = itemView.findViewById<TextView>(R.id.item_explorer_tv_description)
    private val tvDetails = itemView.findViewById<MaterialTextView>(R.id.item_explorer_tv_details)
    private val tvSize = itemView.findViewById<TextView>(R.id.item_explorer_tv_size)
    private val cbBox = itemView.findViewById<MaterialCheckBox>(R.id.item_explorer_cb)
    private val tvError = itemView.findViewById<TextView>(R.id.item_explorer_error_tv)
    private val psProgress = itemView.findViewById<MuonsView>(R.id.item_explorer_ps)

    var onItemActionListener: ExplorerItemBinderActionListener? = null

    private val defaultBoxTintList: ColorStateList by lazy(LazyThreadSafetyMode.NONE) { cbBox.buttonTintList!! }
    private val transparentBoxTintList: ColorStateList

    private val onClickListener: ((View) -> Unit) = {
        onItemActionListener?.onItemClick(item)
    }
    private val onLongClickListener: ((View) -> Boolean) = {
        onItemActionListener?.onItemLongClick(item)
        true
    }
    private val onCheckListener: ((View, Boolean) -> Unit) = { _, checked ->
        if (checked != item.isChecked) {
            onItemActionListener?.onItemCheck(item, checked)
        }
    }
    init {
        if (cbBox.buttonTintList == null) {
            cbBox.isUseMaterialThemeColors = true
        }
        cbBox.isHapticFeedbackEnabled = true

        val stateEnabledChecked = intArrayOf(android.R.attr.state_enabled, android.R.attr.state_checked)
        val stateDisabledChecked = intArrayOf(-android.R.attr.state_enabled, android.R.attr.state_checked)
        val stateEnabledUnchecked = intArrayOf(android.R.attr.state_enabled, -android.R.attr.state_checked)
        val stateDisabledUnchecked = intArrayOf(-android.R.attr.state_enabled, -android.R.attr.state_checked)
        val colorEnabledChecked = defaultBoxTintList.getColorForState(stateEnabledChecked, Color.RED)
        val colorDisabledChecked = defaultBoxTintList.getColorForState(stateDisabledChecked, Color.RED)
        val states = arrayOf(stateEnabledChecked, stateDisabledChecked, stateEnabledUnchecked, stateDisabledUnchecked)
        val colors = intArrayOf(colorEnabledChecked, colorDisabledChecked, Color.TRANSPARENT, Color.TRANSPARENT)
        transparentBoxTintList = ColorStateList(states, colors)
        ivThumbnail.clipToOutline = true

        val size = tvDetails.textSize.toInt()
        dirIcon.setBounds(0, 0, size, size)
        fileIcon.setBounds(0, 0, size, size)
        val dx = when {
            itemView.isRtl() -> 0f
            else -> size.toFloat() / dirIcon.intrinsicWidth * 6
        }
        val dy = size.toFloat() / dirIcon.intrinsicHeight * 6
        dirIcon.set(dx = dx * 2, dy = dy * 2)
        fileIcon.set(dx = dx, dy = dy)
        dirIcon.setTint(tvDetails.currentTextColor)
        fileIcon.setTint(tvDetails.currentTextColor)
        dirIcon.alpha = Alpha.LEVEL_67
        fileIcon.alpha = Alpha.LEVEL_67

        placeholder.setPadding(placeholder.intrinsicSize / 6)
    }

    override fun onBind(item: Node) {
        this.item = item

        itemView.setOnClickListener(onClickListener)
        itemView.setOnLongClickListener(onLongClickListener)
        cbBox.setOnCheckedChangeListener(onCheckListener)

        val thumbnail = (item.content as? NodeContent.File)
            ?.takeIf { item.length > 0 }
            ?.thumbnail
        when (thumbnail) {
            is Thumbnail.FilePath -> Glide
                .with(itemView.context)
                .load(thumbnail.value)
                .placeholder(placeholder)
                .error(LemonDrawable())
                .into(ivThumbnail)
            is Thumbnail.Bitmap -> ivThumbnail.setImageBitmap(thumbnail.value)
            is Thumbnail.Drawable -> ivThumbnail.setImageDrawable(thumbnail.value)
            is Thumbnail.Res -> ivThumbnail.setImageResource(thumbnail.value)
            is Thumbnail.Loading -> ivThumbnail.setImageDrawable(placeholder)
            null -> ivThumbnail.setImageDrawable(null)
        }

        tvName.text = when {
            item.isRoot -> item.getTitle(itemView.resources)
            else -> item.name
        }
        tvName.typeface = if (item.isDirectory) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

        val error = item.error?.let { itemView.resources.getString(it, item.content) }
        tvError.text = error

        cbBox.isChecked = item.isChecked

        val withThumbnail = thumbnail != null
        ivIcon.isVisible = !withThumbnail
        ivThumbnail.isVisible = withThumbnail
        tvError.isVisible = error?.isNotBlank() == true
        psProgress.isVisible = item.withOperation
        cbBox.isInvisible = item.withOperation
        tvDetails.maxWidth = itemView.resources.displayMetrics.widthPixels / 3

        ivIcon.ifVisible {
            ivIcon.setImageResource(item.getIcon())
            ivIcon.alpha = Alpha.enabled(!item.isDirectory || item.isCached)
        }
        tvName.setCompoundDrawablesRelativeWithIntrinsicBounds(if (withThumbnail) item.getIcon() else 0, 0, 0, 0)
    }

    override fun setOnItemActionListener(listener: ExplorerItemActionListener?) {
        onItemActionListener = listener
    }

    override fun bindComposition(composition: ExplorerItemComposition) {
        val string = StringBuilder()
        if (composition.visibleDate) string.append(item.date).append(SPACE)
        if (composition.visibleTime) string.append(item.time).append(SPACE)
        if (composition.visibleAccess) string.append(item.access).append(SPACE)
        if (composition.visibleOwner) string.append(item.owner).append(SPACE)
        if (composition.visibleGroup) string.append(item.group).append(SPACE)
        tvDescription.text = string.toString()
        tvDetails.text = item
            .takeIf { composition.visibleDetails }
            ?.getDetails()
        tvDetails.isVisible = tvDetails.text.isNotEmpty()
        tvSize.text = if (composition.visibleSize) item.size else EMPTY
        cbBox.buttonTintList = if (composition.visibleBox) defaultBoxTintList else transparentBoxTintList
    }

    override fun disableClicks() {
        itemView.isFocusable = false
        itemView.isClickable = false
        itemView.isLongClickable = false
        itemView.setOnClickListener(null)
    }

    override fun hideCheckBox() {
        cbBox.isVisible = false
    }

    override fun setGreyBackgroundColor(visible: Boolean) {
        val color = when {
            visible -> ContextCompat.getColor(itemView.context, R.color.item_explorer_background)
            else -> Color.TRANSPARENT
        }
        itemView.setBackgroundColor(color)
    }

    private fun Node.getDetails(): CharSequence? = when {
        !isDirectory -> content.details
        children == null -> null
        children.isEmpty() -> null
        else -> children.getDirDetails()
    }

    private fun NodeChildren.getDirDetails(): CharSequence {
        val builder = SpannableStringBuilder()
        val dirs = dirs.toString()
        val files = (size - this.dirs).let {
            if (it == 0) Const.EMPTY else it.toString()
        }
        if (this.dirs > 0) {
            builder.append(dirs)
            builder.append("*")
            builder.setSpan(ImageSpan(dirIcon, ALIGN_BASELINE), builder.length.dec(), builder.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.append(Const.SPACE)
        }
        for (i in 0..<(3 - files.length)) {
            builder.append(Const.SPACE)
        }
        builder.append(files)
        builder.append("*")
        builder.setSpan(ImageSpan(fileIcon, ALIGN_BASELINE), builder.length.dec(), builder.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        fileIcon.setVisible(files.isNotEmpty(), restart = false)
        return builder
    }

    interface ExplorerItemBinderActionListener {
        fun onItemClick(item: Node)
        fun onItemLongClick(item: Node)
        fun onItemCheck(item: Node, isChecked: Boolean)
    }
}