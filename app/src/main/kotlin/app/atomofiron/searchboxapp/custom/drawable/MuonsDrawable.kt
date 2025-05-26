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
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.Size
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

private const val PI = Math.PI.toFloat()
private const val START = PI / 2
private const val END = START + PI
private const val DURATION = 512L
private const val EPSILON = 0.05f
private const val ARC_60 = 60f
private const val ROTATE_90 = 90f
private const val ROTATE_180 = 180f
private const val STROKE_WIDTH = 1f / 12
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
    private val rect = RectF()
    private var size = 0f
    private var startX = 0f
    private var startY = 0f

    init {
        paint.isAntiAlias = true
        paint.strokeCap = Paint.Cap.ROUND
        path.fillType = Path.FillType.EVEN_ODD
        animator.duration = DURATION * if (fillCenter) 1 else 2
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.RESTART
        animator.interpolator = LinearInterpolator()
    }

    override fun getIntrinsicWidth(): Int = intrinsicSize.width

    override fun getIntrinsicHeight(): Int = intrinsicSize.height

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        bounds.run {
            size = min(width(), height()).toFloat()
            val dif = (width() - height()) / 2f
            startX = max(0f, dif)
            startY = max(0f, -dif)
        }
    }

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
        val new = animator.animatedValue as Float % PI
        if (new < animValue && !drawn) {
            return anim(false)
        }
        animValue = new
        drawn = false
        updatePath()
        invalidateSelf()
    }

    private fun updatePath() {
        val padding = size * CIRCLES_PADDING
        val area = size - padding * 2
        val offset = padding + startX
        val centerY = bounds.height() / 2f
        val diameter = area / 2
        val radius = diameter / 2
        val cos = cos(animValue) // -1..1
        val x = diameter + radius * cos
        val scaling = sin(animValue) / 2
        val increasing = radius * scaling
        val decreasing = -increasing + increasing * scaling
        path.reset()
        path.addCircle(offset + x, centerY, radius + increasing, Direction.CW)
        path.addCircle(offset + area - x, centerY, radius + decreasing, Direction.CW)
    }

    private fun Canvas.drawMuons() {
        paint.style = Paint.Style.FILL
        drawPath(path, paint)
    }

    private fun Canvas.draw(slugs: Boolean, value: Float) {
        val rotate = ROTATE_180 * value / PI // 0..180
        val cos = cos(value * 2).inc() / 2 // 0..1
        val sweepAngle = if (slugs) max(EPSILON, ARC_60 * (1 - cos)) else EPSILON
        val startAngle = rotate - sweepAngle / 2
        val stroke = size * STROKE_WIDTH
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = when {
            slugs -> stroke + stroke * cos.pow(2) * 2
            else -> max(0f, -stroke + stroke * cos.pow(2) * 4)
        }
        val inset = paint.strokeWidth / 2
        rect.set(bounds)
        rect.inset(startX + inset, startY + inset)
        drawArc(rect, startAngle - ROTATE_90, sweepAngle, false, paint)
        drawArc(rect, startAngle + ROTATE_90, sweepAngle, false, paint)
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