package app.atomofiron.searchboxapp.custom.drawable

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation.BOTTOM_TOP
import android.graphics.drawable.RippleDrawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.core.graphics.ColorUtils
import androidx.core.view.updatePaddingRelative
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.isDarkDeep
import app.atomofiron.common.util.isDarkTheme
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.TextField
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.color
import app.atomofiron.searchboxapp.utils.colorAttr
import app.atomofiron.searchboxapp.utils.drawable
import app.atomofiron.searchboxapp.utils.over
import app.atomofiron.searchboxapp.utils.withAlpha
import com.google.android.material.textfield.TextInputLayout

private fun Context.rippleColor() = colorAttr(MaterialAttr.colorControlHighlight)

private fun Context.rippleColorList() = ColorStateList.valueOf(rippleColor())

fun View.setMenuItemBackground() {
    val drawable = context.drawable(R.drawable.item_menu) as RippleDrawable
    drawable.findDrawableByLayerId(R.id.fill).alpha = Alpha.vodkaInt(context.isDarkDeep())
    background = drawable
}

fun View.setRippleForeground(@DimenRes corners: Int = R.dimen.corner_radius) {
    val mask = GradientDrawable(BOTTOM_TOP, intArrayOf(Color.BLACK, Color.BLACK))
    mask.cornerRadius = resources.getDimension(corners)
    foreground = RippleDrawable(context.rippleColorList(), null, mask)
}

fun View.setStrokedBackground(@DimenRes padding: Int = 0) = setStrokedBackground(padding, padding)

fun View.setStrokedBackground(
    @DimenRes horizontal: Int = 0,
    @DimenRes vertical: Int = 0,
) {
    background = GradientDrawable(BOTTOM_TOP, intArrayOf(0, 0)).apply {
        val color = context.colorSurfaceContainer()
        setStroke(resources.getDimensionPixelSize(R.dimen.stroke_width), color)
        cornerRadius = resources.getDimension(R.dimen.corner_radius)
    }
    clipToOutline = true
    if (horizontal != 0) resources.getDimensionPixelSize(horizontal)
        .also { updatePaddingRelative(start = it, end = it) }
    if (vertical != 0) resources.getDimensionPixelSize(vertical)
        .also { updatePaddingRelative(top = it, bottom = it) }
}

fun TextField.makeHoled(layout: TextInputLayout) = makeFilled(layout, context.colorBackground())

fun TextField.makeToned(layout: TextInputLayout) = makeFilled(layout, context.colorSurfaceContainer())

fun Context.colorSurfaceContainer(): Int {
    val color = colorAttr(MaterialAttr.colorSurfaceContainer)
    return when {
        isDarkDeep() -> color withAlpha Alpha.VODKA over colorAttr(R.attr.colorBackground)
        else -> color
    }
}

@ColorInt
fun Context.surfaceContainerBorder(): Int? {
    return if (isDarkTheme()) {
        val surfaceVariant = colorAttr(MaterialAttr.colorSurfaceVariant)
        ColorUtils.blendARGB(colorSurfaceContainer(), surfaceVariant, 0.2f)
    } else {
        null
    }
}

fun Context.tonedOverlay(color: Int): ColorStateList = ColorStateList.valueOf(color withAlpha Alpha.VODKA over colorBackground())

private fun Context.colorBackground(): Int = colorAttr(R.attr.colorBackground)

@ColorInt
fun Context.coloredContent(@ColorRes colorId: Int, inverse: Boolean): Int {
    val backgroundId = when {
        inverse -> MaterialAttr.colorOnSurfaceInverse
        else -> MaterialAttr.colorOnSurfaceVariant
    }
    return ColorUtils.blendARGB(color(colorId), colorAttr(backgroundId), 0.5f)
}
