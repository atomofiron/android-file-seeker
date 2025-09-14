package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.text.style.ImageSpan.ALIGN_BASELINE
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.extension.debugRequire
import app.atomofiron.common.util.ifVisible
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemExplorerBinding
import app.atomofiron.searchboxapp.custom.LemonDrawable
import app.atomofiron.searchboxapp.custom.drawable.MuonsDrawable
import app.atomofiron.searchboxapp.custom.drawable.colorSurfaceContainer
import app.atomofiron.searchboxapp.custom.drawable.translated
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeChildren
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.makeDeepest
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.makeOpened
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootViewHolder.Companion.getTitle
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.colorAttr
import app.atomofiron.searchboxapp.utils.getString
import app.atomofiron.searchboxapp.utils.isRtl
import com.bumptech.glide.Glide

private const val SPACE = " "
private const val EMPTY = ""

class ExplorerItemBinderImpl private constructor(
    private val itemView: View,
    private val binding: ItemExplorerBinding,
    isOpened: Boolean,
) : ExplorerItemBinder {

    private lateinit var item: Node
    private var isDeepest: Boolean? = null

    private var dirDrawable = ContextCompat.getDrawable(itemView.context, R.drawable.ic_folder)!!.mutate().translated()
    private var fileDrawable = ContextCompat.getDrawable(itemView.context, R.drawable.ic_file)!!.mutate().translated()
    private val placeholder = MuonsDrawable(itemView.context)
    private val dirTint = ColorStateList.valueOf(itemView.context.colorAttr(MaterialAttr.colorPrimary))
    private val fileTint = ColorStateList.valueOf(itemView.context.colorAttr(MaterialAttr.colorAccent))

    private var onItemActionListener: ExplorerItemBinderActionListener? = null

    private val defaultBoxTintList: ColorStateList
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

    constructor(itemView: View, isOpened: Boolean) : this(itemView, ItemExplorerBinding.bind(itemView), isOpened)

    constructor(binding: ItemExplorerBinding, isOpened: Boolean = false) : this(binding.root, binding, isOpened)

    init {
        bindState(isOpened, isDeepest = false)
        if (binding.checkBox.buttonTintList == null) {
            binding.checkBox.isUseMaterialThemeColors = true
        }
        defaultBoxTintList = binding.checkBox.buttonTintList!!
        transparentBoxTintList = transparentCheckbox(defaultBoxTintList)

        val size = binding.details.textSize.toInt()
        dirDrawable.setBounds(0, 0, size, size)
        fileDrawable.setBounds(0, 0, size, size)
        val dx = when {
            itemView.isRtl() -> 0f
            else -> size.toFloat() / dirDrawable.intrinsicWidth * 6
        }
        val dy = size.toFloat() / dirDrawable.intrinsicHeight * 6
        dirDrawable.set(dx = dx * 2, dy = dy * 2)
        fileDrawable.set(dx = dx, dy = dy)
        dirDrawable.setTint(binding.details.currentTextColor)
        fileDrawable.setTint(binding.details.currentTextColor)
        dirDrawable.alpha = Alpha.LEVEL_67
        fileDrawable.alpha = Alpha.LEVEL_67

        placeholder.setPadding(placeholder.intrinsicSize / 6)
        binding.thumbnail.clipToOutline = true
    }

    override fun bind(item: Node) {
        this.item = item

        itemView.setOnClickListener(onClickListener)
        itemView.setOnLongClickListener(onLongClickListener)
        binding.checkBox.setOnCheckedChangeListener(onCheckListener)

        val thumbnail = (item.content as? NodeContent.File)?.thumbnail
        when (thumbnail) {
            is Thumbnail.FilePath -> Glide
                .with(itemView.context)
                .load(thumbnail.value)
                .placeholder(placeholder)
                .error(LemonDrawable())
                .into(binding.thumbnail)
            is Thumbnail.Bitmap -> binding.thumbnail.setImageBitmap(thumbnail.value)
            is Thumbnail.Drawable -> binding.thumbnail.setImageDrawable(thumbnail.value)
            is Thumbnail.Res -> binding.thumbnail.setImageResource(thumbnail.value)
            is Thumbnail.Loading -> binding.thumbnail.setImageDrawable(placeholder)
            null -> binding.thumbnail.setImageDrawable(null)
        }

        binding.title.text = when {
            item.isRoot -> item.getTitle(itemView.resources)
            else -> item.name
        }
        binding.title.typeface = if (item.isDirectory) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

        val error = item.error?.let { itemView.resources.getString(it, item.content) }
        binding.error.text = error

        binding.checkBox.isChecked = item.isChecked

        val withThumbnail = thumbnail != null
        binding.icon.isVisible = !withThumbnail
        binding.thumbnail.isVisible = withThumbnail
        binding.error.isVisible = error?.isNotBlank() == true
        binding.progress.isVisible = item.withOperation
        binding.checkBox.isInvisible = item.withOperation
        binding.details.maxWidth = itemView.resources.displayMetrics.widthPixels / 3

        val iconTint = if (item.isDirectory) dirTint else fileTint
        binding.icon.ifVisible {
            binding.icon.setImageResource(item.getIcon())
            binding.icon.imageTintList = iconTint
            binding.icon.alpha = Alpha.enabled(!item.isDirectory || item.isCached)
        }
        binding.title.setCompoundDrawablesRelativeWithIntrinsicBounds(if (withThumbnail) item.getIcon() else 0, 0, 0, 0)
        TextViewCompat.setCompoundDrawableTintList(binding.title, iconTint)
        debugRequire(item.isOpened == (isDeepest != null)) { "isOpened change: ${item.isOpened}, $isDeepest" }
        bindState(item.isOpened, item.isDeepest)
    }

    private fun transparentCheckbox(defaultBoxTintList: ColorStateList): ColorStateList {
        val stateEnabledChecked = intArrayOf(android.R.attr.state_enabled, android.R.attr.state_checked)
        val stateDisabledChecked = intArrayOf(-android.R.attr.state_enabled, android.R.attr.state_checked)
        val stateEnabledUnchecked = intArrayOf(android.R.attr.state_enabled, -android.R.attr.state_checked)
        val stateDisabledUnchecked = intArrayOf(-android.R.attr.state_enabled, -android.R.attr.state_checked)
        val colorEnabledChecked = defaultBoxTintList.getColorForState(stateEnabledChecked, Color.MAGENTA)
        val colorDisabledChecked = defaultBoxTintList.getColorForState(stateDisabledChecked, Color.MAGENTA)
        val states = arrayOf(stateEnabledChecked, stateDisabledChecked, stateEnabledUnchecked, stateDisabledUnchecked)
        val colors = intArrayOf(colorEnabledChecked, colorDisabledChecked, Color.TRANSPARENT, Color.TRANSPARENT)
        return ColorStateList(states, colors)
    }

    private fun bindState(isOpened: Boolean, isDeepest: Boolean) {
        when {
            !isOpened -> Unit // always is opened or not
            isDeepest == this.isDeepest -> Unit
            isDeepest -> binding.makeDeepest()
            else -> binding.makeOpened()
        }
        this.isDeepest = isDeepest.takeIf { isOpened }
    }

    override fun setOnItemActionListener(listener: ExplorerItemBinderActionListener?) {
        onItemActionListener = listener
    }

    override fun bindComposition(composition: ExplorerItemComposition) {
        val string = StringBuilder()
        if (composition.visibleDate) string.append(item.date).append(SPACE)
        if (composition.visibleTime) string.append(item.time).append(SPACE)
        if (composition.visibleAccess) string.append(item.access).append(SPACE)
        if (composition.visibleOwner) string.append(item.owner).append(SPACE)
        if (composition.visibleGroup) string.append(item.group).append(SPACE)
        binding.description.text = string.toString()
        binding.details.text = item
            .takeIf { composition.visibleDetails }
            ?.getDetails()
        binding.details.isVisible = binding.details.text.isNotEmpty()
        binding.size.text = if (composition.visibleSize) item.size else EMPTY
        binding.checkBox.buttonTintList = if (composition.visibleBox) defaultBoxTintList else transparentBoxTintList
    }

    override fun disableClicks() {
        binding.checkBox.isEnabled = false
        itemView.setOnClickListener(null)
        itemView.setOnLongClickListener(null)
        itemView.background = null
        itemView.isFocusable = false
        itemView.isClickable = false
        itemView.isLongClickable = false
    }

    override fun hideCheckBox() {
        binding.checkBox.isVisible = false
    }

    override fun showAlternatingBackground(visible: Boolean) {
        val color = when {
            visible -> itemView.context.colorSurfaceContainer()
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
            builder.setSpan(ImageSpan(dirDrawable, ALIGN_BASELINE), builder.length.dec(), builder.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.append(Const.SPACE)
            for (i in 0..<(3 - files.length)) {
                builder.append(Const.SPACE)
            }
        }
        builder.append(files)
        builder.append("*")
        builder.setSpan(ImageSpan(fileDrawable, ALIGN_BASELINE), builder.length.dec(), builder.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        fileDrawable.setVisible(files.isNotEmpty(), restart = false)
        return builder
    }

    interface ExplorerItemBinderActionListener {
        fun onItemClick(item: Node)
        fun onItemLongClick(item: Node)
        fun onItemCheck(item: Node, isChecked: Boolean)
    }
}