package app.atomofiron.searchboxapp.custom.drawable

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import androidx.annotation.ColorInt
import androidx.annotation.Px

class HybridTextLayoutDrawable(
    original: Drawable,
    @Px focusedStroke: Int,
    @ColorInt focusedColor: Int,
    radius: Float,
) : LayerDrawable(arrayOf(original, strokeDrawable(focusedStroke, focusedColor, radius)))

private fun strokeDrawable(focusedStroke: Int, focusedColor: Int, radius: Float): Drawable = GradientDrawable().apply {
    val colors = ColorStateList(
        arrayOf(intArrayOf(android.R.attr.state_focused), intArrayOf(0)),
        intArrayOf(focusedColor, Color.TRANSPARENT),
    )
    setStroke(focusedStroke, colors)
    cornerRadius = radius
}
