package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.RippleDrawable
import android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.text.style.ImageSpan.ALIGN_BASELINE
import android.view.View
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.extension.debugRequire
import app.atomofiron.common.util.extension.unit
import app.atomofiron.common.util.ifVisible
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemExplorerBinding
import app.atomofiron.searchboxapp.custom.LemonDrawable
import app.atomofiron.searchboxapp.custom.ProgressLineDrawable
import app.atomofiron.searchboxapp.custom.drawable.MuonsDrawable
import app.atomofiron.searchboxapp.custom.drawable.colorSurfaceContainer
import app.atomofiron.searchboxapp.custom.drawable.translated
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeChildren
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.explorer.NodeOperation
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.model.other.get
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.makeDeepest
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.makeOpened
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootViewHolder.Companion.getTitle
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.audio.AudioCover
import app.atomofiron.searchboxapp.utils.colorAttr
import app.atomofiron.searchboxapp.utils.context
import app.atomofiron.searchboxapp.utils.isRtl
import app.atomofiron.searchboxapp.utils.remember
import app.atomofiron.searchboxapp.utils.resources
import app.atomofiron.searchboxapp.utils.toUni
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

private const val SPACE = " "
private const val EMPTY = ""

class ExplorerItemBinder(
    private val binding: ItemExplorerBinding,
    private val isOpened: Boolean = false,
) {
    private val context = binding.context
    private val resources = binding.resources

    private lateinit var item: Node
    private var isDeepest: Boolean? = null

    private var dirDrawable = ContextCompat.getDrawable(context, R.drawable.ic_folder)!!.mutate().translated()
    private var fileDrawable = ContextCompat.getDrawable(context, R.drawable.ic_file)!!.mutate().translated()
    private val placeholder = MuonsDrawable(context)
    private val dirTint = ColorStateList.valueOf(context.colorAttr(MaterialAttr.colorPrimary))
    private val fileTint = ColorStateList.valueOf(context.colorAttr(MaterialAttr.colorAccent))
    private val lemon = LemonDrawable()
    private val progressDrawable = ProgressLineDrawable(context)
    val rippleDrawable = binding.root.background as? RippleDrawable

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
    private val onCheckListener: ((CompoundButton, Boolean) -> Unit) = { view, checked ->
        if (checked != item.isChecked && onItemActionListener?.onItemCheck(item, checked) == false) {
            view.isChecked = item.isChecked
        }
    }
    private val bitmapListener = BitmapListener()

    constructor(itemView: View, isOpened: Boolean = false) : this(ItemExplorerBinding.bind(itemView), isOpened)

    init {
        bindStyle(isOpened, isDeepest = false)
        if (binding.checkBox.buttonTintList == null) {
            binding.checkBox.isUseMaterialThemeColors = true
        }
        defaultBoxTintList = binding.checkBox.buttonTintList!!
        transparentBoxTintList = transparentCheckbox(defaultBoxTintList)

        val size = binding.details.textSize.toInt()
        dirDrawable.setBounds(0, 0, size, size)
        fileDrawable.setBounds(0, 0, size, size)
        val dx = when {
            binding.root.isRtl() -> 0f
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
        rippleDrawable?.addLayer(progressDrawable)
    }

    fun bind(item: Node) = binding.run {
        this@ExplorerItemBinder.item = item

        binding.root.setOnClickListener(onClickListener)
        binding.root.setOnLongClickListener(onLongClickListener)
        checkBox.setOnCheckedChangeListener(onCheckListener)

        when (val tn = (item.content as? NodeContent.File)?.thumbnail) {
            is Thumbnail.FilePath -> thumbnail.remember(item.ref) {
                Glide.with(context)
                    .loadFor(item)
                    .placeholder(placeholder)
                    .error(lemon)
                    .into(thumbnail)
            }
            is Thumbnail.Bitmap -> thumbnail.setImageBitmap(tn.value)
            is Thumbnail.Drawable -> thumbnail.setImageDrawable(tn.value)
            is Thumbnail.Res -> thumbnail.setImageResource(tn.value)
            is Thumbnail.Loading -> thumbnail.setImageDrawable(placeholder)
            null -> thumbnail.setImageDrawable(null)
        }
        apply(hasThumbnail = item.content.instantThumbnail())

        title.text = when {
            item.isRoot -> item.getTitle(resources)
            else -> item.name
        }
        title.typeface = if (item.isDirectory) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

        val errorText = item.error?.getString(item.content)
        error.text = errorText

        checkBox.isChecked = item.isChecked

        error.isVisible = errorText?.isNotBlank() == true
        progress.isVisible = item.inProgress
        checkBox.isInvisible = item.inProgress
        details.maxWidth = resources.displayMetrics.widthPixels / 3

        val iconTint = if (item.isDirectory) dirTint else fileTint
        icon.ifVisible {
            icon.setImageResource(item.getIcon())
            icon.imageTintList = iconTint
            icon.alpha = Alpha.enabled(!item.isDirectory || item.isCached)
        }
        TextViewCompat.setCompoundDrawableTintList(title, iconTint)
        debugRequire(item.isOpened == isOpened) { "${item.name} isOpened change: $isOpened -> ${item.isOpened}, isDeepest=$isDeepest" }
        bindStyle(item.isOpened, item.isDeepest)
        val copying = item.state.operation as? NodeOperation.Copying
        progressDrawable.setVisible(copying?.isSource == false)
        copying?.let { progressDrawable.set(it.progress) }
    }.unit()

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

    private fun bindStyle(isOpened: Boolean, isDeepest: Boolean) {
        when {
            !isOpened -> Unit // always is opened or not
            isDeepest == this.isDeepest -> Unit
            isDeepest -> binding.makeDeepest()
            else -> binding.makeOpened()
        }
        this.isDeepest = isDeepest.takeIf { isOpened }
    }

    fun setOnItemActionListener(listener: ExplorerItemBinderActionListener?) {
        onItemActionListener = listener
    }

    fun bindComposition(composition: ExplorerItemComposition) {
        val filteredOut = item.children?.filteredOut
        binding.description.text = when (filteredOut) {
            null, 0 -> StringBuilder().run {
                if (composition.visibleDate) append(item.date).append(SPACE)
                if (composition.visibleTime) append(item.time).append(SPACE)
                if (composition.visibleAccess) append(item.access).append(SPACE)
                if (composition.visibleOwner) append(item.owner).append(SPACE)
                if (composition.visibleGroup) append(item.group).append(SPACE)
                toString()
            }
            else -> resources.getQuantityString(R.plurals.files_filtered, filteredOut, filteredOut)
        }
        binding.details.text = item
            .takeIf { composition.visibleDetails }
            ?.getDetails()
        binding.details.isVisible = binding.details.text.isNotEmpty()
        binding.size.text = if (composition.visibleSize) item.size else EMPTY
        binding.checkBox.buttonTintList = if (composition.visibleBox) defaultBoxTintList else transparentBoxTintList
    }

    fun disableClicks() {
        binding.checkBox.isEnabled = false
        binding.root.setOnClickListener(null)
        binding.root.setOnLongClickListener(null)
        binding.root.background = null
        binding.root.isFocusable = false
        binding.root.isClickable = false
        binding.root.isLongClickable = false
    }

    fun hideCheckBox() {
        binding.checkBox.isVisible = false
    }

    fun showAlternatingBackground(visible: Boolean) {
        val color = when {
            visible -> context.colorSurfaceContainer()
            else -> Color.TRANSPARENT
        }
        binding.root.setBackgroundColor(color)
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

    private fun RequestManager.loadFor(item: Node): RequestBuilder<out Any> {
        return when (item.content) {
            is NodeContent.Music -> asBitmap()
                .load(AudioCover(item.ref.string))
                .addListener(bitmapListener)
            else -> load(item.ref.string)
        }
    }

    private fun NodeContent?.instantThumbnail() = when {
        this == null -> false
        this !is NodeContent.File -> false
        thumbnail == null -> false
        thumbnail !is Thumbnail.FilePath -> true
        item.content is NodeContent.Music -> false
        else -> true
    }

    private fun apply(hasThumbnail: Boolean) {
        binding.icon.isVisible = !hasThumbnail
        binding.thumbnail.isVisible = hasThumbnail
        binding.title.setCompoundDrawablesRelativeWithIntrinsicBounds(if (hasThumbnail) item.getIcon() else 0, 0, 0, 0)
    }

    interface ExplorerItemBinderActionListener {
        fun onItemClick(item: Node)
        fun onItemLongClick(item: Node)
        /** @return false if is not allowed */
        fun onItemCheck(item: Node, toChecked: Boolean): Boolean
    }

    private inner class BitmapListener : RequestListener<Bitmap> {

        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Bitmap?>,
            isFirstResource: Boolean,
        ): Boolean {
            apply(hasThumbnail = false)
            return false
        }

        override fun onResourceReady(
            resource: Bitmap,
            model: Any,
            target: Target<Bitmap?>?,
            dataSource: DataSource,
            isFirstResource: Boolean,
        ): Boolean {
            apply(hasThumbnail = true)
            return false
        }
    }

    fun NodeError.getString(content: NodeContent? = null): String = resources[toUni(content)]
}
