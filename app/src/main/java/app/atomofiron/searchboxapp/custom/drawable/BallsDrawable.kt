package app.atomofiron.searchboxapp.custom.drawable

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import app.atomofiron.common.util.WeakDrawableCallback
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import com.google.android.material.R as MaterialR
import kotlin.math.min

class BallsDrawable private constructor(context: Context) : Drawable(), ValueAnimator.AnimatorUpdateListener, View.OnAttachStateChangeListener {
    companion object {
        private const val DURATION = 512L

        fun ImageView.setBallsDrawable(): BallsDrawable {
            val drawable = BallsDrawable(context)
            drawable.callback = WeakDrawableCallback(this)
            setImageDrawable(drawable)
            removeOnAttachStateChangeListener(drawable)
            addOnAttachStateChangeListener(drawable)
            if (isAttachedToWindow) drawable.onViewAttachedToWindow(this)
            return drawable
        }
    }

    private var oneBall = false
    private val paintCircle = Paint()
    private val paintBall = Paint()
    private var animValue = 0.0
    private var isAttachedToWindow = false

    private val ballCirclePath = Path()
    private val animator = ValueAnimator.ofFloat(0f, Math.PI.toFloat())

    init {
        val colorAccent = context.findColorByAttr(MaterialR.attr.colorAccent)
        paintCircle.color = colorAccent
        paintBall.color = colorAccent
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

    override fun onViewAttachedToWindow(view: View) {
        isAttachedToWindow = true
        animator.removeUpdateListener(this)
        animator.addUpdateListener(this)
        updateAnimation()
    }

    override fun onViewDetachedFromWindow(view: View) {
        isAttachedToWindow = false
        animator.removeUpdateListener(this)
        updateAnimation()
    }

    private fun updateAnimation() {
        val enable = isAttachedToWindow// && isVisible
        when {
            !enable -> animator.pause()
            animator.isPaused -> animator.resume()
            !animator.isStarted -> animator.start()
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
        var size = Math.min(width, height).toFloat()
        size -= size % 2
        val radius = size / 6
        val radiusRotate = size / (if (oneBall) 4 else 3)
        val radiusMask = size / 2
        val sin = Math.sin(animValue) * radiusRotate
        val cos = Math.cos(animValue) * radiusRotate
        val x1 = centerX + cos
        val y1 = centerY + sin

        if (oneBall) {
            canvas.drawCircle(centerX, centerY, radiusMask, paintCircle)
            canvas.drawCircle(x1.toFloat(), y1.toFloat(), radius, paintBall)
        } else {
            val x2 = centerX - cos
            val y2 = centerY - sin
            canvas.clipPath(ballCirclePath)
            canvas.drawCircle(x1.toFloat(), y1.toFloat(), radius, paintBall)
            canvas.drawCircle(x2.toFloat(), y2.toFloat(), radius, paintBall)
        }
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        animValue = (animator.animatedValue as Float).toDouble()
        invalidateSelf()
    }

    override fun setAlpha(alpha: Int) = Unit

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    @Deprecated("", ReplaceWith(""))
    override fun getOpacity(): Int = PixelFormat.UNKNOWN

    fun setColor(color: Int) {
        paintCircle.color = color
        paintBall.color = color
    }
}