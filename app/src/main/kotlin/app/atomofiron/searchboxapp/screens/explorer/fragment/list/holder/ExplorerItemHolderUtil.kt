package app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.ColorUtils
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemExplorerBinding
import app.atomofiron.fileseeker.databinding.ItemExplorerSeparatorBinding
import app.atomofiron.searchboxapp.utils.Alpha

const val TAG_EXPLORER_OPENED_ITEM = "TAG_EXPLORER_OPENED_ITEM"

fun ItemExplorerSeparatorBinding.makeSeparator() {
    val background = root.context.findColorByAttr(MaterialAttr.colorOutline)
    val content = root.context.findColorByAttr(MaterialAttr.colorSurface)
    val ripple = ColorUtils.setAlphaComponent(content, Alpha.RIPPLE_INT)
    val cornerRadius = root.resources.getDimension(R.dimen.explorer_border_corner_radius)
    val drawable = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(background, background))
    drawable.cornerRadii = FloatArray(8) { cornerRadius }
    root.background = RippleDrawable(ColorStateList.valueOf(ripple), drawable, null)
    val filter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(content, BlendModeCompat.SRC_IN)
    icon.colorFilter = filter
    title.setTextColor(content)
}

fun ItemExplorerBinding.makeOpened() {
    val background = root.context.findColorByAttr(MaterialAttr.colorOutline)
    val content = root.context.findColorByAttr(MaterialAttr.colorSurface)
    val buttonIcon = root.context.findColorByAttr(MaterialAttr.colorOnSurface)
    makeOpposite(background, content, buttonIcon)
}

fun ItemExplorerBinding.makeDeepest() {
    val background = root.context.findColorByAttr(MaterialAttr.colorSecondary)
    val content = root.context.findColorByAttr(MaterialAttr.colorSurface)
    val buttonIcon = root.context.findColorByAttr(MaterialAttr.colorOnSurface)
    makeOpposite(background, content, buttonIcon)
}

private fun ItemExplorerBinding.makeOpposite(
    background: Int,
    content: Int,
    buttonIcon: Int,
) {
    val cornerRadius = root.resources.getDimension(R.dimen.explorer_border_corner_radius)
    val drawable = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(background, background))
    drawable.cornerRadii = FloatArray(8) {
        if (it < 5) cornerRadius else cornerRadius
    }
    val rippleMask = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(Color.BLACK, Color.BLACK))
    rippleMask.cornerRadius = cornerRadius
    val rippleColor = ColorUtils.setAlphaComponent(content, Alpha.RIPPLE_INT)
    val rippleColorList = ColorStateList.valueOf(rippleColor)
    root.background = RippleDrawable(rippleColorList, drawable, rippleMask)
    val filter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(content, BlendModeCompat.SRC_IN)
    itemExplorerIvIcon.colorFilter = filter
    itemExplorerCb.buttonTintList = ColorStateList.valueOf(content)
    itemExplorerCb.buttonIconTintList = ColorStateList.valueOf(buttonIcon)
    itemExplorerTvTitle.setTextColor(content)
    itemExplorerTvSize.setTextColor(content)
    itemExplorerTvDescription.setTextColor(content)
    itemExplorerTvDetails.setTextColor(content)
    itemExplorerErrorTv.setTextColor(content)
    itemExplorerPs.setTint(content)
    root.tag = TAG_EXPLORER_OPENED_ITEM
}