package app.atomofiron.searchboxapp.custom

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.view.isVisible

class LemonDrawable : Drawable() {
    override fun draw(canvas: Canvas) = Unit
    override fun setAlpha(alpha: Int) = Unit
    override fun setColorFilter(colorFilter: ColorFilter?) = Unit
    override fun getOpacity(): Int = PixelFormat.TRANSPARENT
    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        (callback as? View)?.isVisible = false
    }
}
