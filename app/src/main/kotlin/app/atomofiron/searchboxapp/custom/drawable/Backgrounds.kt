package app.atomofiron.searchboxapp.custom.drawable

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation.BOTTOM_TOP
import android.graphics.drawable.RippleDrawable
import android.view.View
import androidx.annotation.DimenRes
import androidx.core.view.updatePaddingRelative
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.findBooleanByAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.TextField
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.drawable
import com.google.android.material.textfield.TextInputLayout

fun View.setMenuItemBackground() {
    val drawable = context.drawable(R.drawable.item_menu) as RippleDrawable
    val deepBlack = context.findBooleanByAttr(R.attr.isBlackDeep)
    val darkTheme = resources.getBoolean(R.bool.isNightTheme)
    drawable.findDrawableByLayerId(R.id.fill).alpha = Alpha.visibleInt(!darkTheme || !deepBlack)
    drawable.findDrawableByLayerId(R.id.stroke).alpha = Alpha.visibleInt(darkTheme && deepBlack)
    background = drawable
}

fun View.setStrokedBackground(
    @DimenRes horizontal: Int = 0,
    @DimenRes vertical: Int = 0,
) {
    background = GradientDrawable(BOTTOM_TOP, intArrayOf(0, 0)).apply {
        val color = context.findColorByAttr(MaterialAttr.colorSurfaceContainer)
        setStroke(resources.getDimensionPixelSize(R.dimen.stroke_width), color)
        cornerRadius = resources.getDimension(R.dimen.corner_radius)
    }
    clipToOutline = true
    if (horizontal != 0) resources.getDimensionPixelSize(horizontal)
        .also { updatePaddingRelative(start = it, end = it) }
    if (vertical != 0) resources.getDimensionPixelSize(vertical)
        .also { updatePaddingRelative(top = it, bottom = it) }
}

fun TextField.makeHoled(layout: TextInputLayout) = makeFilled(layout, android.R.attr.colorBackground)

fun TextField.makeToned(layout: TextInputLayout) = makeFilled(layout, MaterialAttr.colorSurfaceContainer)

