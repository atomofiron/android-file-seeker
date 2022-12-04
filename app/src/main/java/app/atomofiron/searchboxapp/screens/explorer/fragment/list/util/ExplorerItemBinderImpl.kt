package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.checkbox.MaterialCheckBox
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.BallsView
import app.atomofiron.searchboxapp.model.explorer.*
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerItemActionListener
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.getString
import app.atomofiron.searchboxapp.model.explorer.Node.Companion.toUniqueId
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.getExternalStorageDirectory

class ExplorerItemBinderImpl(
    private val itemView: View,
) : ExplorerItemBinder {
    companion object {
        private const val BYTE_LETTER = "B"
        private const val SPACE = " "
        private const val EMPTY = ""
    }

    private lateinit var item: Node

    private val ivIcon = itemView.findViewById<ImageView>(R.id.item_explorer_iv_icon)
    private val ivThumbnail = itemView.findViewById<ImageView>(R.id.item_explorer_iv_thumbnail)
    private val tvName = itemView.findViewById<TextView>(R.id.item_explorer_tv_title)
    private val tvDescription = itemView.findViewById<TextView>(R.id.item_explorer_tv_description)
    private val tvDate = itemView.findViewById<TextView>(R.id.item_explorer_tv_date)
    private val tvSize = itemView.findViewById<TextView>(R.id.item_explorer_tv_size)
    private val cbBox = itemView.findViewById<MaterialCheckBox>(R.id.item_explorer_cb)
    private val tvError = itemView.findViewById<TextView>(R.id.item_explorer_error_tv)
    private val psProgress = itemView.findViewById<BallsView>(R.id.item_explorer_ps)

    var onItemActionListener: ExplorerItemBinderActionListener? = null

    private val rootsAliases = HashMap<Int, Int>()

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
        itemView.context.getExternalStorageDirectory()?.let {
            rootsAliases[it.toUniqueId()] = R.string.internal_storage
        }
        rootsAliases[Const.ROOT.toUniqueId()] = R.string.root

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
    }

    override fun onBind(item: Node) {
        this.item = item

        itemView.setOnClickListener(onClickListener)
        itemView.setOnLongClickListener(onLongClickListener)
        cbBox.setOnCheckedChangeListener(onCheckListener)

        ivIcon.setImageResource(item.defineIcon())
        ivIcon.alpha = if (item.isDirectory && !item.isCached) Const.ALPHA_DISABLED else Const.ALPHA_ENABLED
        val thumbnail = (item.content as? NodeContent.File)?.thumbnail
        ivThumbnail.setImageDrawable(thumbnail)

        val aliasId = rootsAliases[item.uniqueId]
        tvName.text = when (aliasId) {
            null -> item.name
            else -> itemView.context.getString(aliasId)
        }
        tvName.typeface = if (item.isDirectory) Typeface.DEFAULT_BOLD else Typeface.DEFAULT

        val error = item.error?.let { itemView.resources.getString(it, item.content) }
        tvError.text = error

        cbBox.isChecked = item.isChecked

        val withThumbnail = thumbnail != null
        ivIcon.isVisible = !withThumbnail
        ivThumbnail.isVisible = withThumbnail
        tvError.isVisible = error != null
        psProgress.isVisible = item.withOperation
        when {
            item.withOperation -> cbBox.isInvisible = true
            else -> cbBox.isVisible = true
        }
    }

    override fun setOnItemActionListener(listener: ExplorerItemActionListener?) {
        onItemActionListener = listener
    }

    override fun bindComposition(composition: ExplorerItemComposition) {
        val string = StringBuilder()
        if (composition.visibleAccess) {
            string.append(item.access).append(SPACE)
        }
        if (composition.visibleOwner) {
            string.append(item.owner).append(SPACE)
        }
        if (composition.visibleGroup) {
            string.append(item.group)
        }
        tvDescription.text = string.toString()
        string.clear()
        if (composition.visibleDate) {
            string.append(item.date)
        }
        if (composition.visibleTime) {
            string.append(SPACE).append(item.time)
        }
        tvDate.text = string.toString()
        tvSize.text = when {
            !composition.visibleSize -> EMPTY
            !item.isFile -> EMPTY
            item.size.isBlank() -> EMPTY
            else -> item.size + BYTE_LETTER
        }
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

    private fun Node.defineIcon(): Int {
        return when (val content = content) {
            is NodeContent.Unknown -> R.drawable.ic_explorer_unknown
            is NodeContent.Link -> R.drawable.ic_explorer_link
            is NodeContent.File -> content.getIcon()
            is NodeContent.Directory -> content.getIcon(isEmpty)
        }
    }

    interface ExplorerItemBinderActionListener {
        fun onItemClick(item: Node)
        fun onItemLongClick(item: Node)
        fun onItemCheck(item: Node, isChecked: Boolean)
    }
}