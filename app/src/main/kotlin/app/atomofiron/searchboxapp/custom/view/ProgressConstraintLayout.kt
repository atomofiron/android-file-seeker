package app.atomofiron.searchboxapp.custom.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader.TileMode.CLAMP
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.graphics.withTranslation
import androidx.core.view.isVisible
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.withAlpha

private const val INDETERMINATE = -2f
private const val DURATION = 3000L

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

    private var red = context.getColor(R.color.red_lite)
    private var green = context.getColor(R.color.green_lite)
    private var blue = context.getColor(R.color.blue_lite)
    private var yellow = context.getColor(R.color.yellow_lite)
    private var pink = context.getColor(R.color.pink_lite)
    private val animator = ValueAnimator.ofFloat(0f, 1f)
    private val paint = Paint()
    private var progress = INDETERMINATE
    private var wave = 0f
    private var color: Color = Pink
    private val colors = intArrayOf(0, 0)
    private var ignoreId = 0
    private var progressStart = 0f
    private var progressEnd = 0f

    init {
        context.withStyledAttributes(attrs, R.styleable.ProgressLayout, defStyleAttr, defStyleRes) {
            ignoreId = getResourceId(R.styleable.ProgressLayout_ignoreId, ignoreId)
        }
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        paint.strokeCap = Paint.Cap.ROUND
        paint.style = Paint.Style.FILL
        animator.repeatCount = ValueAnimator.INFINITE
    }

    fun makeDarker() {
        red = red.darker()
        green = green.darker()
        blue = blue.darker()
        yellow = yellow.darker()
        pink = pink.darker()
    }

    fun setProgress(color: Color, value: Float = INDETERMINATE) {
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateShader()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        resetProgress()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val width = (right - left).toFloat()
        progressStart = if (isRtl) width else 0f
        progressEnd = if (isRtl) 0f else width
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
            canvas.drawProgress(Alpha.SMALL_INT)
            val saved = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
            super.dispatchDraw(canvas)
            canvas.drawProgress(Alpha.VISIBLE_INT)
            canvas.restoreToCount(saved)
            canvas.drawIgnored()
        } else {
            super.dispatchDraw(canvas)
        }
        if (animator.isStarted) canvas.drawWave()
    }

    private fun Canvas.drawProgress(alpha: Int) {
        paint.color = paint.color withAlpha alpha
        val end = progressStart + (progressEnd - progressStart) * progress
        drawRect(progressStart, 0f, end, height.toFloat(), paint)
    }

    private fun Canvas.drawWave() {
        paint.color = paint.color withAlpha (Alpha.SMALL * (1f - wave))
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
}

fun Int.darker(): Int = 0xff.shl(24) + (red / 2).shl(16) + (green / 2).shl(8) + (blue / 2)
