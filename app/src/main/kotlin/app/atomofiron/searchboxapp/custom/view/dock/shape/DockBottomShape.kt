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
        if (style.translucent) {
            return
        }
        if (style.fill != Color.TRANSPARENT) {
            paint.style = Paint.Style.FILL
            paint.color = style.fill
            canvas.drawPath(path, paint)
        }
    }

    override fun onBoundsChange(bounds: Rect) = updatePath()

    @Suppress("DEPRECATION")
    override fun getOutline(outline: Outline) = when {
        style.translucent -> Unit
        Android.R -> outline.setPath(path)
        else -> Unit
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
        rect.set(bounds)
        var x = rect.left
        var y = rect.bottom
        reset()
        moveTo(x, y)
        y = rect.top
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
        x = rect.right
        y = rect.top
        corner(x, y, left = false, top = true, clockWise = true, corners)
        y = rect.bottom
        lineTo(x, y)
    }
}