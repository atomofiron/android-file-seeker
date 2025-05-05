package app.atomofiron.searchboxapp.custom.view.dock.shape

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.extension.corner

class DockBottomShape(
    private val corners: Float,
    private val stroke: Int,
    private var style: DockStyle = DockStyle.Stub,
    private var notch: DockNotch? = null,
) : Drawable() {

    private val paint = Paint()
    private val path = Path()
    private val rect = RectF()

    init {
        paint.color = Color.TRANSPARENT
        paint.isAntiAlias = true
    }

    fun setNotch(notch: DockNotch?): DockBottomShape {
        if (notch != this.notch) {
            this.notch = notch
            updatePath()
            invalidateSelf()
        }
        return this
    }

    fun setStyle(style: DockStyle) {
        if (style != this.style) {
            this.style = style
            updatePath()
            invalidateSelf()
        }
    }

    override fun draw(canvas: Canvas) {
        if (style.transparent) {
            return
        }
        if (style.fill != Color.TRANSPARENT) {
            paint.style = Paint.Style.FILL
            paint.color = style.fill
            canvas.drawPath(path, paint)
        }
        if (stroke != Color.TRANSPARENT && style.strokeWidth > 0f) {
            paint.style = Paint.Style.STROKE
            paint.color = stroke
            paint.strokeWidth = style.strokeWidth
            canvas.drawPath(path, paint)
        }
    }

    override fun onBoundsChange(bounds: Rect) = updatePath()

    @Suppress("DEPRECATION")
    override fun getOutline(outline: Outline) = when {
        style.transparent -> Unit
        Android.R -> outline.setPath(path)
        else -> outline.setConvexPath(path)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(filter: ColorFilter?) {
        paint.setColorFilter(filter)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    private fun updatePath() = path.run {
        val inset = when (stroke) {
            Color.TRANSPARENT -> 0f
            else -> style.strokeWidth / 2
        }
        rect.set(bounds)
        var x = rect.left + inset
        var y = rect.bottom
        reset()
        moveTo(x, y)
        y = rect.top + inset
        corner(x, y, top = true, left = true, clockWise = true, radius = corners)
        notch?.let { notch ->
            x = rect.left + rect.width() / 2 - notch.radius
            corner(x, y, left = false, top = true, clockWise = true, corners)
            y += notch.height
            corner(x, y, left = true, top = false, clockWise = false, notch.radius)
            x += notch.width
            corner(x, y, left = false, top = false, clockWise = false, notch.radius)
            y -= notch.height
            corner(x, y, left = true, top = true, clockWise = true, corners)
        }
        x = rect.right - inset
        y = rect.top
        corner(x, y, left = false, top = true, clockWise = true, corners)
        y = rect.bottom
        lineTo(x, y)
    }
}