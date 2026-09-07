package app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.ColorUtils
import app.atomofiron.searchboxapp.utils.colorAttr
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemExplorerBinding
import app.atomofiron.fileseeker.databinding.ItemExplorerSeparatorBinding
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.ColorStates

const val TAG_EXPLORER_OPENED_ITEM = "TAG_EXPLORER_OPENED_ITEM"

fun ItemExplorerSeparatorBinding.makeSeparator() {
    val background = root.context.colorAttr(MaterialAttr.colorOutline)
    val content = root.context.colorAttr(MaterialAttr.colorSurface)
    val ripple = ColorUtils.setAlphaComponent(content, Alpha.RIPPLE_INT)
    val cornerRadius = root.resources.getDimension(R.dimen.explorer_border_corner_radius)
    val drawable = GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, intArrayOf(background, background))
    drawable.cornerRadii = FloatArray(8) { cornerRadius }
    root.background = RippleDrawable(ColorStates(default = ripple), drawable, null)
    val filter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(content, BlendModeCompat.SRC_IN)
    icon.colorFilter = filter
    title.setTextColor(content)
}

fun ItemExplorerBinding.makeOpened() {
    val background = root.context.colorAttr(MaterialAttr.colorOutline)
    val content = root.context.colorAttr(MaterialAttr.colorSurface)
    val buttonIcon = root.context.colorAttr(MaterialAttr.colorOnSurface)
    makeOpposite(background, content, buttonIcon)
}

fun ItemExplorerBinding.makeDeepest() {
    val background = root.context.colorAttr(MaterialAttr.colorSecondary)
    val content = root.context.colorAttr(MaterialAttr.colorSurface)
    val buttonIcon = root.context.colorAttr(MaterialAttr.colorOnSurface)
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
    val rippleColorList = ColorStates(default = rippleColor)
    root.background = RippleDrawable(rippleColorList, drawable, rippleMask)
    val filter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(content, BlendModeCompat.SRC_IN)
    icon.colorFilter = filter
    checkBox.buttonTintList = ColorStates(default = content)
    checkBox.buttonIconTintList = ColorStates(default = buttonIcon)
    title.setTextColor(content)
    size.setTextColor(content)
    description.setTextColor(content)
    details.setTextColor(content)
    error.setTextColor(content)
    progress.setTint(content)
    root.tag = TAG_EXPLORER_OPENED_ITEM
}