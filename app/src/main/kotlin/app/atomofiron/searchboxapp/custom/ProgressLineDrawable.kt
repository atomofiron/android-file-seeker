package app.atomofiron.searchboxapp.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.colorAttr

class ProgressLineDrawable(context: Context) : Drawable() {

    private val paint = Paint()
    private val radius = context.resources.getDimension(R.dimen.progress_line_width) / 2
    private val padding = context.resources.getDimension(R.dimen.padding_common)
    private var progress = 0f

    init {
        paint.strokeWidth = radius * 2
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = context.colorAttr(MaterialAttr.colorPrimaryInverse)
    }

    fun setVisible(visible: Boolean) = super.setVisible(visible, false)

    fun set(progress: Float) {
        this.progress = progress
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        if (!isVisible) return
        val y = bounds.bottom - radius
        val dx = (bounds.width() - padding) * progress
        paint.alpha = Alpha.LEVEL_30
        canvas.drawLine(padding, y, bounds.width() - padding, y, paint)
        paint.alpha = Alpha.VISIBLE_INT
        canvas.drawLine(padding, y, padding + dx, y, paint)
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) = Unit

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit
}
