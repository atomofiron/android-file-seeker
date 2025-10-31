package app.atomofiron.searchboxapp.utils

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

private class Clipping(private val radius: Float) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) = outline.setRoundRect(0, -radius.toInt(), view.width, view.height, radius)
}
