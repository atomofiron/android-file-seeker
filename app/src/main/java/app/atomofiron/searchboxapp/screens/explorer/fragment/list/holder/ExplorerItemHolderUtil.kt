package app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.ItemExplorerBinding
import app.atomofiron.searchboxapp.databinding.ItemExplorerSeparatorBinding

const val TAG_EXPLORER_OPENED_ITEM = "TAG_EXPLORER_OPENED_ITEM"

fun ItemExplorerSeparatorBinding.makeSeparator() {
    val background = root.context.findColorByAttr(R.attr.colorOutline)
    val content = root.context.findColorByAttr(R.attr.colorSurface)
    val cornerRadius = root.resources.getDimension(R.dimen.explorer_border_corner_radius)
    val drawable = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(background, background))
    drawable.cornerRadii = FloatArray(8) { cornerRadius }
    root.background = RippleDrawable(ColorStateList.valueOf(content), drawable, null)
    val filter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(content, BlendModeCompat.SRC_IN)
    itemExplorerIvIcon.colorFilter = filter
    itemExplorerTvTitle.setTextColor(content)
}

fun ItemExplorerBinding.makeOpened() {
    val background = root.context.findColorByAttr(R.attr.colorOutline)
    val content = root.context.findColorByAttr(R.attr.colorSurface)
    val buttonIcon = root.context.findColorByAttr(R.attr.colorOnSurface)
    makeOpposite(background, content, buttonIcon, topRadius = true, bottomRadius = true)
}

fun ItemExplorerBinding.makeOpenedCurrent() {
    val background = root.context.findColorByAttr(R.attr.colorSecondary)
    val content = root.context.findColorByAttr(R.attr.colorSurface)
    val buttonIcon = root.context.findColorByAttr(R.attr.colorOnSurface)
    makeOpposite(background, content, buttonIcon, topRadius = true)
}

private fun ItemExplorerBinding.makeOpposite(
    background: Int,
    content: Int,
    buttonIcon: Int,
    topRadius: Boolean = false,
    bottomRadius: Boolean = false,
) {
    val cornerRadius = root.resources.getDimension(R.dimen.explorer_border_corner_radius)
    val drawable = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(background, background))
    drawable.cornerRadii = FloatArray(8) {
        when {
            it < 5 && topRadius -> cornerRadius
            it > 4 && bottomRadius -> cornerRadius
            else -> 0f
        }
    }
    val contentColorList = ColorStateList.valueOf(content)
    root.background = RippleDrawable(contentColorList, drawable, null)
    val filter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(content, BlendModeCompat.SRC_IN)
    itemExplorerIvIcon.colorFilter = filter
    itemExplorerCb.buttonTintList = contentColorList
    itemExplorerCb.buttonIconTintList = ColorStateList.valueOf(buttonIcon)
    itemExplorerTvTitle.setTextColor(content)
    itemExplorerTvSize.setTextColor(content)
    itemExplorerTvDescription.setTextColor(content)
    itemExplorerErrorTv.setTextColor(content)
    itemExplorerPs.setColor(content)
    root.tag = TAG_EXPLORER_OPENED_ITEM
}