package app.atomofiron.searchboxapp.custom.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.createBitmap
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.findBooleanByAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

private const val PRESS = HapticFeedbackConstants.KEYBOARD_TAP
private const val RELEASE = HapticFeedbackConstants.CLOCK_TICK

private const val RELEASED = 0f
private const val PRESSED = (Math.PI / 2).toFloat()

private const val FULL = 255
private const val GLOW_DURATION = 256L

class JoystickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val icon = ContextCompat.getDrawable(context, R.drawable.ic_esc)!!
    private val padding = resources.getDimension(R.dimen.joystick_padding)

    private val paintBlur = Paint()
    private val glowingPaint = Paint()
    private val paint = Paint()
    private var shadowColor = ColorUtils.setAlphaComponent(Color.BLACK, 80)
    private var glowColor = 0

    private val animator = ValueAnimator.ofFloat()
    private val blurRadius = resources.getDimension(R.dimen.joystick_elevation)
    private var trackTouchEvent = false
    private var pressure = 0f
    private val maxRadius get() = min(width, height) / 2 - padding
    private val minRadius get() = maxRadius - blurRadius / 2

    private var composition = JoystickComposition.Default
    private var bitmap = createBitmap(1, 1, Bitmap.Config.ALPHA_8)
    private var blurCanvas = Canvas(bitmap)
    private val offset = IntArray(2)

    init {
        paint.isAntiAlias = true
        animator.duration = GLOW_DURATION
        animator.addUpdateListener { animator ->
            val value = animator.animatedValue as Float
            pressure = sin(value.toDouble()).toFloat()
            invalidate()
        }
        val hw = icon.intrinsicWidth / 2
        val hv = icon.intrinsicHeight / 2
        icon.setBounds(-hw, -hv, hw, hv)
        paintBlur.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val size = min(right - left, bottom - top)
        if (size != 0 && bitmap.width != size) {
            bitmap.recycle()
            bitmap = createBitmap(size, size)
            blurCanvas = Canvas(bitmap)
        }
    }

    fun setComposition(composition: JoystickComposition? = this.composition) {
        composition ?: return
        this.composition = composition
        val isDark = context.findBooleanByAttr(R.attr.isDarkTheme)
        val colorPrimary = context.findColorByAttr(MaterialAttr.colorPrimary)

        val circleColor = when {
            composition.overrideTheme -> composition.color(isDark)
            else -> colorPrimary
        }
        paint.color = circleColor
        glowColor = when {
            composition.overrideTheme -> composition.glow(isDark)
            else -> composition.glow(isDark, colorPrimary)
        }

        val black = ContextCompat.getColor(context, R.color.black)
        val white = ContextCompat.getColor(context, R.color.white)
        val contrastBlack = ColorUtils.calculateContrast(black, circleColor)
        val contrastWhite = ColorUtils.calculateContrast(white, circleColor)
        val iconColor = when {
            contrastBlack > contrastWhite -> black
            else -> white
        }
        icon.colorFilter = PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> press()
            MotionEvent.ACTION_MOVE -> if (trackTouchEvent && event.outside()) release()
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> if (trackTouchEvent) release()
        }
        return super.onTouchEvent(event)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val width = bitmap.width
        val height = bitmap.height
        val cx = width / 2f
        val cy = height / 2f
        val radius = fromToBy(maxRadius, minRadius, pressure)
        blurCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        blurCanvas.drawCircle(cx, cy, radius, paint)
        val glowing = bitmap.extractAlpha(paintBlur, offset)
        val dx = offset.first().toFloat()
        val dy = offset.last() + blurRadius * (1f - pressure) / 2f
        glowingPaint.color = ColorUtils.compositeColors(ColorUtils.setAlphaComponent(glowColor, (FULL * pressure).toInt()), shadowColor)
        canvas.drawBitmap(glowing, dx, dy, glowingPaint)
        canvas.drawCircle(cx, cy, radius, paint)

        val scale = radius / maxRadius
        canvas.translate(cx, cy)
        canvas.scale(scale, scale)
        icon.draw(canvas)
    }

    private fun fromToBy(from: Float, to: Float, by: Float) = from + (to - from) * by

    private fun MotionEvent.outside(): Boolean {
        val dx = (x - width / 2)
        val dy = (y - height / 2)
        return dx.pow(2) + dy.pow(2) > maxRadius.pow(2)
    }

    private fun press() {
        trackTouchEvent = true
        if (composition.withHaptic) performHapticFeedback(PRESS)
        play(RELEASED, PRESSED)
        pressure = 1f
        invalidate()
    }

    private fun release() {
        trackTouchEvent = false
        if (composition.withHaptic) performHapticFeedback(RELEASE)
        play(PRESSED, RELEASED)
    }

    private fun play(from: Float, to: Float) {
        animator.cancel()
        animator.setFloatValues(from, to)
        animator.start()
    }
}