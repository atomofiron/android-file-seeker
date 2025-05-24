package app.atomofiron.searchboxapp.custom.drawable

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.util.Size
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// todo create the new animation

private const val START = 0f

class BallsDrawable private constructor(
    color: Int,
    private val intrinsicSize: Size,
) : Drawable(), ValueAnimator.AnimatorUpdateListener {
    companion object {
        private const val DURATION = 512L

        operator fun invoke(context: Context): BallsDrawable {
            val color = context.findColorByAttr(MaterialAttr.colorAccent)
            return BallsDrawable(color, Size(100, 100))
        }

        fun ImageView.setBallsDrawable(): BallsDrawable {
            val intrinsicSize = resources.getDimensionPixelSize(R.dimen.icon_size)
            val color = context.findColorByAttr(MaterialAttr.colorAccent)
            val drawable = BallsDrawable(color, Size(intrinsicSize, intrinsicSize))
            setImageDrawable(drawable)
            return drawable
        }
    }

    private var oneBall = false
    private val paintCircle = Paint()
    private val paintBall = Paint()
    private var animValue = START
    private var invalidated = true

    private val ballCirclePath = Path()
    private val animator = ValueAnimator.ofFloat(0f, Math.PI.toFloat())

    init {
        paintCircle.color = color
        paintBall.color = color
        if (oneBall) {
            paintBall.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        animator.duration = DURATION
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.RESTART
        animator.interpolator = LinearInterpolator()

        paintBall.isAntiAlias = true
        paintCircle.isAntiAlias = true
    }

    override fun getIntrinsicWidth(): Int = intrinsicSize.width

    override fun getIntrinsicHeight(): Int = intrinsicSize.height

    private fun anim(value: Boolean) {
        if (!value) {
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

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)

        val width = (right - left).toFloat()
        val height = (bottom - top).toFloat()
        val centerX = width / 2
        val centerY = height / 2
        var size = min(width, height)
        size -= size % 2
        val radius = size / 6
        val radiusMask = size / 2
        ballCirclePath.addCircle(centerX, centerY - radiusMask - radius, radiusMask, Path.Direction.CW)
        ballCirclePath.addCircle(centerX, centerY + radiusMask + radius, radiusMask, Path.Direction.CW)
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width()
        val height = bounds.height()
        val centerX = width.toFloat() / 2
        val centerY = height.toFloat() / 2
        var size = min(width, height).toFloat()
        size -= size % 2
        val radius = size / 6
        val radiusRotate = size / (if (oneBall) 4 else 3)
        val radiusMask = size / 2
        val sin = sin(animValue) * radiusRotate
        val cos = cos(animValue) * radiusRotate
        val x1 = centerX + cos
        val y1 = centerY + sin

        if (oneBall) {
            canvas.drawCircle(centerX, centerY, radiusMask, paintCircle)
            canvas.drawCircle(x1, y1, radius, paintBall)
        } else {
            val x2 = centerX - cos
            val y2 = centerY - sin
            canvas.clipPath(ballCirclePath)
            canvas.drawCircle(x1, y1, radius, paintBall)
            canvas.drawCircle(x2, y2, radius, paintBall)
        }
        invalidated = false
        anim(true)
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val new = animator.animatedValue as Float
        if (new < animValue && invalidated) {
            return anim(false)
        }
        animValue = new
        invalidateSelf()
        invalidated = true
    }

    override fun setAlpha(alpha: Int) = Unit

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paintBall.setColorFilter(colorFilter)
        paintCircle.setColorFilter(colorFilter)
    }

    override fun setTintList(tint: ColorStateList?) {
        super.setTintList(tint)
        setColor(tint?.defaultColor ?: Color.TRANSPARENT)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.UNKNOWN

    fun setColor(color: Int) {
        paintCircle.color = color
        paintBall.color = color
    }
}