package app.atomofiron.searchboxapp.custom.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader.TileMode.CLAMP
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.withTranslation
import androidx.core.view.isVisible
import app.atomofiron.common.util.extension.debugRequire
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.withAlpha
import app.atomofiron.searchboxapp.custom.drawable.coloredContent
import kotlin.math.cos

private const val INDETERMINATE = -2f
private const val DURATION = 3000L
private const val HALF_PI = Math.PI.toFloat() / 2

class ProgressConstraintLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes), ValueAnimator.AnimatorUpdateListener {

    sealed interface Color
    object Red : Color
    object Green : Color
    object Blue : Color
    object Yellow : Color
    object Pink : Color

    private var red = 0
    private var green = 0
    private var blue = 0
    private var yellow = 0
    private var pink = 0
    private val animator = ValueAnimator.ofFloat(0f, 1f)
    private val paint = Paint()
    private var progress = INDETERMINATE
    private var wave = 0f
    private var color: Color = Pink
    private val colors = intArrayOf(0, 0)
    private var ignoreId = 0
    private var progressPadding = 0f
    private val progressStart get() = if (isRtl) width.toFloat() else 0f
    private val progressEnd get() = if (isRtl) 0f else width.toFloat()
    private var clipPath = Path()

    init {
        context.withStyledAttributes(attrs, R.styleable.ProgressLayout, defStyleAttr, defStyleRes) {
            ignoreId = getResourceId(R.styleable.ProgressLayout_ignoreId, ignoreId)
        }
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        paint.strokeCap = Paint.Cap.ROUND
        paint.style = Paint.Style.FILL
        animator.repeatCount = ValueAnimator.INFINITE
    }

    fun init(inverseColors: Boolean, padding: Float) {
        progressPadding = padding
        red = context.coloredContent(R.color.red_lite, inverseColors)
        green = context.coloredContent(R.color.green_lite, inverseColors)
        blue = context.coloredContent(R.color.blue_lite, inverseColors)
        yellow = context.coloredContent(R.color.yellow_lite, inverseColors)
        pink = context.coloredContent(R.color.pink_lite, inverseColors)
    }

    fun setProgress(color: Color, value: Float = INDETERMINATE) {
        debugRequire(red != 0)
        progress = when (value) {
            INDETERMINATE -> value
            else -> value.coerceIn(0f, 1f)
        }
        this.color = color
        updateShader()
        anim(true)
        invalidate()
    }

    fun resetProgress() {
        anim(false)
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        resetProgress()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (!changed) {
            return
        }
        updateShader()
        val width = (right - left).toFloat()
        val height = (bottom - top).toFloat()
        (background as? RippleDrawable)
            ?.findDrawableByLayerId(android.R.id.mask)
            ?.let { it as? GradientDrawable }
            ?.run {
                clipPath.reset()
                clipPath.addRoundRect(0f, 0f, width, height, cornerRadius, cornerRadius, Path.Direction.CW)
            }
    }

    private fun anim(value: Boolean) = when (value) {
        animator.isStarted -> Unit
        true -> {
            animator.addUpdateListener(this)
            animator.start()
        }
        false -> {
            animator.removeUpdateListener(this)
            animator.cancel()
        }
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        wave = (System.currentTimeMillis() % DURATION) / DURATION.toFloat()
        invalidate()
    }

    private fun updateShader() {
        val color = when (color) {
            Red -> red
            Green -> green
            Blue -> blue
            Yellow -> yellow
            Pink -> pink
        }
        colors[0] = color
        colors[1] = color
        paint.color = color
        paint.shader = LinearGradient(0f, 0f, 0f, height.toFloat(), colors, null, CLAMP)
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (animator.isStarted && progress != INDETERMINATE) {
            canvas.drawProgress(Alpha.SMALL_INT, clip = true)
            val saved = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
            super.dispatchDraw(canvas)
            canvas.drawProgress(Alpha.VISIBLE_INT, clip = false)
            canvas.restoreToCount(saved)
            canvas.drawIgnored()
        } else {
            super.dispatchDraw(canvas)
        }
        if (animator.isStarted) canvas.drawWave()
    }

    private fun Canvas.drawProgress(alpha: Int, clip: Boolean) {
        paint.color = paint.color withAlpha alpha
        val start = if (progressStart == 0f) progressPadding else (progressStart - progressPadding)
        var end = if (progressEnd == 0f) progressPadding else (progressEnd - progressPadding)
        end = start + (end - start) * progress
        if (clip) clip()
        drawRect(progressStart, 0f, end, height.toFloat(), paint)
    }

    private fun Canvas.drawWave() {
        paint.color = paint.color withAlpha (Alpha.LITE * cos(wave * HALF_PI))
        clip()
        drawRect(progressStart, 0f, progressEnd * wave, height.toFloat(), paint)
    }

    private fun Canvas.drawIgnored() {
        val view = this@ProgressConstraintLayout.takeIf { ignoreId != 0 }
            ?.findViewById<View>(ignoreId)
            ?.takeIf { it.isVisible }
            ?: return
        withTranslation(view.x, view.y) {
            view.draw(this)
        }
    }

    private fun Canvas.clip() {
        if (!clipPath.isEmpty) clipPath(clipPath)
    }
}
