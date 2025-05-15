package app.atomofiron.searchboxapp.custom.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import app.atomofiron.common.util.Android

class PathDrawable(
    private val path: Path,
    private val fill: Int,
    private val stroke: Int,
    strokeWidth: Float,
) : Drawable() {
    companion object {
        fun fill(path: Path, color: Int) = PathDrawable(path, fill = color, stroke = Color.TRANSPARENT, strokeWidth = 0f)
        fun stroke(path: Path, color: Int, strokeWidth: Float) = PathDrawable(path, fill = Color.TRANSPARENT, stroke = color, strokeWidth)
    }

    private val paint = Paint()

    init {
        paint.strokeWidth = strokeWidth
    }

    // needed for shadow to work
    override fun getOutline(outline: Outline) = when {
        Android.R -> outline.setPath(path)
        else -> Unit
    }

    override fun draw(canvas: Canvas) {
        if (fill != Color.TRANSPARENT) {
            paint.color = fill
            paint.style = Paint.Style.FILL
            canvas.drawPath(path, paint)
        }
        if (stroke != Color.TRANSPARENT && paint.strokeWidth > 0f) {
            paint.color = stroke
            paint.style = Paint.Style.STROKE
            canvas.drawPath(path, paint)
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
