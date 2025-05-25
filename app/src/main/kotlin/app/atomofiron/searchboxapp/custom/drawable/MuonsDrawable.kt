package app.atomofiron.searchboxapp.custom.drawable

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Path.Direction
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.util.Size
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

private const val DURATION = 512L
private const val START = 0f
private const val PI = Math.PI.toFloat()
private const val END = PI
private const val EPSILON = 0.05f
private const val ARC_60 = 60f
private const val OFFSET_90 = 90f
private const val OFFSET_180 = 180f
private const val STROKE_WIDTH = 1f / 16
private const val CIRCLES_PADDING = 1f / 6

class MuonsDrawable private constructor(
    private val defaultColor: Int,
    private val fillCenter: Boolean,
    private val intrinsicSize: Size,
) : Drawable(), ValueAnimator.AnimatorUpdateListener {
    companion object {

        operator fun invoke(color: Int = Color.MAGENTA, fillCenter: Boolean = true): MuonsDrawable {
            return MuonsDrawable(color, fillCenter, Size(1, 1))
        }

        operator fun invoke(context: Context, fillCenter: Boolean = true): MuonsDrawable {
            val color = context.findColorByAttr(MaterialAttr.colorAccent)
            val intrinsicSize = context.resources.getDimensionPixelSize(R.dimen.icon_size)
            return MuonsDrawable(color, fillCenter, Size(intrinsicSize, intrinsicSize))
        }

        fun ImageView.setMuonsDrawable(fillCenter: Boolean = true): MuonsDrawable {
            val drawable = MuonsDrawable(context, fillCenter)
            setImageDrawable(drawable)
            return drawable
        }
    }

    private val paint = Paint()
    private var animValue = START
    private var drawn = true

    private val path = Path()
    private var tint: ColorStateList? = null
    private val animator = ValueAnimator.ofFloat(START, END)

    init {
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.ROUND
        animator.duration = DURATION * if (fillCenter) 1 else 2
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.RESTART
        animator.interpolator = LinearInterpolator()
        path.fillType = Path.FillType.EVEN_ODD
    }

    override fun getIntrinsicWidth(): Int = intrinsicSize.width

    override fun getIntrinsicHeight(): Int = intrinsicSize.height

    private fun anim(value: Boolean) {
        if (!value) {
            // memory leaks in ValueAnimator
            animator.removeUpdateListener(this)
            animator.pause()
        } else if (animator.isPaused) {
            animator.addUpdateListener(this)
            animator.resume()
        } else if (!animator.isStarted) {
            animator.addUpdateListener(this)
            animator.start()
        }
    }

    override fun draw(canvas: Canvas) {
        if (fillCenter) {
            canvas.draw(slugs = true, animValue)
            canvas.drawMuons()
        } else {
            canvas.draw(slugs = false, animValue)
            canvas.draw(slugs = false, animValue - PI / 2)
        }
        drawn = true
        anim(true)
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val new = animator.animatedValue as Float
        if (new < animValue && !drawn) {
            return anim(false)
        }
        animValue = new
        drawn = false
        invalidateSelf()

        var width = bounds.width().toFloat()
        val inset = width * CIRCLES_PADDING
        width -= inset * 2
        val centerY = bounds.height() / 2f
        val radius = width / 4
        val x = radius + width / 2 * (cos(new) / 2 + 0.5f)
        val scale = radius * sin(new) / 2
        path.reset()
        path.addCircle(inset + x, centerY, radius + scale, Direction.CW)
        path.addCircle(inset + width - x, centerY, radius - scale, Direction.CW)
    }

    private fun Canvas.drawMuons() {
        paint.style = Paint.Style.FILL
        drawPath(path, paint)
    }

    private fun Canvas.draw(slugs: Boolean, value: Float) {
        val width = bounds.width()
        val height = bounds.height()
        val offset = OFFSET_180 * value / END // 0..180
        val cos = cos(value * 2).inc() / 2 // 0..1
        val sweepAngle = if (slugs) max(EPSILON, ARC_60 * (1 - cos)) else EPSILON
        val startAngle = OFFSET_90 + offset - sweepAngle / 2
        val stroke = width * STROKE_WIDTH
        paint.strokeWidth = when {
            slugs -> stroke + stroke * cos.pow(2) * 3
            else -> max(0f, -stroke + stroke * cos.pow(2) * 6)
        }
        val inset = paint.strokeWidth / 2
        paint.style = Paint.Style.STROKE
        drawArc(inset, inset, width - inset, height - inset, startAngle, sweepAngle, false, paint)
        drawArc(inset, inset, width - inset, height - inset, startAngle + OFFSET_180, sweepAngle, false, paint)

    }

    override fun setTintList(tint: ColorStateList?) {
        this.tint = tint
        invalidateSelf()
    }

    override fun onStateChange(state: IntArray): Boolean {
        invalidateSelf()
        return super.onStateChange(state)
    }

    override fun setAlpha(alpha: Int) = paint.setAlpha(alpha)

    override fun getColorFilter(): ColorFilter? = paint.colorFilter

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.setColorFilter(colorFilter)
        super.invalidateSelf()
    }

    override fun invalidateSelf() {
        paint.color = tint
            ?.run { getColorForState(state, defaultColor) }
            ?: defaultColor
        super.invalidateSelf()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.UNKNOWN
}