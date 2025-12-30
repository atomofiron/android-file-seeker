package app.atomofiron.searchboxapp.custom

import android.animation.ValueAnimator
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private const val HalfPi = PI.toFloat() / 2

class ProgressLineDrawable(context: Context) : Drawable(), ValueAnimator.AnimatorUpdateListener {

    private val paint = Paint()
    private val strokeWidth = context.resources.getDimension(R.dimen.progress_line_width)
    private val padding = context.resources.getDimension(R.dimen.padding_common)
    private var progress = 0f
    private var offset = 0f
    private var visible = false
    private val animator = ValueAnimator.ofFloat(0f, 2f)

    init {
        paint.strokeWidth = strokeWidth
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = context.colorAttr(MaterialAttr.colorPrimaryInverse)
        animator.duration = 1000
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.RESTART
    }

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean = false

    fun setVisible(visible: Boolean) {
        if (visible != this.visible && visible == super<Drawable>.isVisible) {
            invalidateSelf()
        }
        super.setVisible(visible, false)
        if (visible != this.visible) {
            this.visible = visible
            if (visible) {
                animator.addUpdateListener(this)
                animator.start()
            } else {
                animator.removeUpdateListener(this)
                animator.cancel()
            }
        }
    }

    fun set(progress: Float) {
        this.progress = progress
    }

    override fun draw(canvas: Canvas) {
        if (!visible) return
        val y = bounds.bottom - strokeWidth / 2
        val edge = padding + paint.strokeWidth / 2
        canvas.translate(edge, 0f)
        val range = bounds.width() - edge * 2
        canvas.drawTrack(range, y)
        canvas.drawIndeterminate(range, y)
        canvas.drawProgress(range, y)
    }

    private fun Canvas.drawTrack(range: Float, y: Float) {
        paint.alpha = Alpha.LEVEL_20
        drawLine(0f, y, range, y, paint)
    }

    private fun Canvas.drawIndeterminate(range: Float, y: Float) {
        paint.alpha = Alpha.LEVEL_20
        var start = max(offset - 1, 0f)
        var end = min(offset, 1f)
        start = sin(start * HalfPi) * range
        end = (1 - cos(end * HalfPi)) * range
        paint.alpha = (Alpha.LEVEL_20 * Alpha((end - start) / strokeWidth)).toInt()
        drawLine(start, y, end, y, paint)
    }

    private fun Canvas.drawProgress(range: Float, y: Float) {
        paint.alpha = Alpha.VISIBLE_INT
        drawLine(0f, y, range * progress, y, paint)
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        offset = animation.animatedValue as Float
        invalidateSelf()
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) = Unit

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit
}
