package app.atomofiron.searchboxapp.custom.drawable

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.Drawable
import android.util.Size
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import app.atomofiron.common.util.WeakDrawableCallback
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.fileseeker.R
import kotlin.math.min

private enum class Flag {
    Visible, Attached, Set
}

class BallsDrawable private constructor(
    context: Context,
    private val intrinsicSize: Size,
) : Drawable(), ValueAnimator.AnimatorUpdateListener, View.OnAttachStateChangeListener {
    companion object {
        private const val DURATION = 512L

        fun ImageView.setBallsDrawable(): BallsDrawable {
            val intrinsicSize = resources.getDimensionPixelSize(R.dimen.icon_size)
            val drawable = BallsDrawable(context, Size(intrinsicSize, intrinsicSize))
            drawable.callback = WeakDrawableCallback(this)
            setImageDrawable(drawable)
            drawable.checkSet()
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

    private val ballCirclePath = Path()
    private val animator = ValueAnimator.ofFloat(0f, Math.PI.toFloat())
    private val flags = mutableSetOf<Flag>()

    init {
        val colorAccent = context.findColorByAttr(MaterialAttr.colorAccent)
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

    override fun getIntrinsicWidth(): Int = intrinsicSize.width

    override fun getIntrinsicHeight(): Int = intrinsicSize.height

    override fun onViewAttachedToWindow(view: View) {
        flags.add(Flag.Attached)
        checkFlags()
    }

    override fun onViewDetachedFromWindow(view: View) {
        flags.remove(Flag.Attached)
        checkFlags()
    }

    private fun checkFlags() {
        if (flags.size < Flag.entries.size) {
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
        if (animValue == 0.0) {
            checkSet()
        }
    }

    override fun setAlpha(alpha: Int) = Unit

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    @Deprecated("", ReplaceWith(""))
    override fun getOpacity(): Int = PixelFormat.UNKNOWN

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        when {
            visible -> flags.add(Flag.Visible)
            else -> flags.remove(Flag.Visible)
        }
        checkFlags()
        return super.setVisible(visible, restart)
    }

    fun setColor(color: Int) {
        paintCircle.color = color
        paintBall.color = color
    }

    fun checkSet() {
        when (isSet()) {
            flags.contains(Flag.Set) -> return
            true -> flags.add(Flag.Set)
            false -> flags.remove(Flag.Set)
        }
        checkFlags()
    }

    private fun Drawable.isSet(): Boolean {
        return when (val ref = callback) {
            is WeakDrawableCallback -> (ref.view as? ImageView)?.drawable === this
            is ImageView -> ref.drawable === this
            is Drawable -> ref === this || ref.isSet()
            else -> false
        }
    }
}