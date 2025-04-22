package app.atomofiron.searchboxapp.custom.drawable

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import app.atomofiron.common.util.Android

class PathDrawable(
    private val path: Path,
    color: Int,
) : Drawable() {

    private val paint = Paint()

    init {
        paint.style = Paint.Style.FILL
        paint.color = color
    }

    // needed for shadow to work
    override fun getOutline(outline: Outline) = when {
        Android.R -> outline.setPath(path)
        else -> outline.setConvexPath(path)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
