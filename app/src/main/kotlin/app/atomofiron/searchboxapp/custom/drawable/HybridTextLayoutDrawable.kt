package app.atomofiron.searchboxapp.custom.drawable

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import androidx.annotation.ColorInt
import androidx.annotation.Px
import app.atomofiron.searchboxapp.utils.ColorStates

class HybridTextLayoutDrawable(
    original: Drawable,
    @Px focusedStroke: Int,
    @ColorInt focusedColor: Int,
    radius: Float,
) : LayerDrawable(arrayOf(original, strokeDrawable(focusedStroke, focusedColor, radius)))

private fun strokeDrawable(focusedStroke: Int, focusedColor: Int, radius: Float): Drawable = GradientDrawable().apply {
    val colors = ColorStates(default = Color.TRANSPARENT) {
        focusedColor.add(focused)
    }
    setStroke(focusedStroke, colors)
    cornerRadius = radius
}
