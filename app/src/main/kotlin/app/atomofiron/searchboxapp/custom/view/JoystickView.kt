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
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.createBitmap
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.findBooleanByAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.preference.JoystickHaptic
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import app.atomofiron.searchboxapp.utils.HAPTIC_HEAVY
import app.atomofiron.searchboxapp.utils.HAPTIC_LITE
import app.atomofiron.searchboxapp.utils.HAPTIC_NO
import app.atomofiron.searchboxapp.utils.performHapticEffect
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

private const val RELEASED = 0f
private const val PRESSED = (Math.PI / 2).toFloat()

private const val FULL = 255
private const val GLOW_DURATION = 256L

fun JoystickHaptic.effect(press: Boolean) = when (this) {
    JoystickHaptic.None -> HAPTIC_NO
    JoystickHaptic.Lite -> if (press) HAPTIC_LITE else HAPTIC_NO
    JoystickHaptic.Double -> HAPTIC_LITE
    JoystickHaptic.Heavy -> if (press) HAPTIC_HEAVY else HAPTIC_HEAVY
}

class JoystickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val icon = ContextCompat.getDrawable(context, R.drawable.ic_esc)!!
    private val padding = resources.getDimensionPixelSize(R.dimen.joystick_padding)

    private val paintBlur = Paint()
    private val glowingPaint = Paint()
    private val paint = Paint()
    private var shadowColor = ColorUtils.setAlphaComponent(Color.BLACK, 80)
    private var glowColor = 0

    private val animator = ValueAnimator.ofFloat()
    private val shadowRadius = resources.getDimension(R.dimen.joystick_elevation)
    private val glowRadius = shadowRadius * 2
    private var trackTouches = false
    private var pressure = 0f
    private val maxRadius get() = min(width, height) / 2f - padding
    private val minRadius get() = maxRadius - shadowRadius / 2

    private var composition = JoystickComposition.Default
    private var blurBitmap = createBitmap(1, 1, Bitmap.Config.ALPHA_8)
    private var blurCanvas = Canvas(blurBitmap)
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
        paintBlur.maskFilter = BlurMaskFilter(shadowRadius, BlurMaskFilter.Blur.NORMAL)
        setPaddingRelative(padding, padding, padding, padding)
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val size = min(right - left, bottom - top)
        if (size != 0 && blurBitmap.width != size) {
            blurBitmap.recycle()
            blurBitmap = createBitmap(size, size)
            blurCanvas = Canvas(blurBitmap)
        }
    }

    fun setComposition(composition: JoystickComposition? = this.composition) {
        composition ?: return
        this.composition = composition
        val isDark = context.findBooleanByAttr(R.attr.isDarkTheme)
        val colorPrimary = context.findColorByAttr(MaterialAttr.colorTertiaryContainer)

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
            !composition.overrideTheme -> context.findColorByAttr(MaterialAttr.colorOnTertiaryContainer)
            contrastBlack > contrastWhite -> black
            else -> white
        }
        icon.colorFilter = PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
        invalidate()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> if (!event.outside()) press()
            MotionEvent.ACTION_MOVE -> if (trackTouches && event.outside()) release()
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> when {
                trackTouches -> release()
                else -> return true
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val width = blurBitmap.width
        val height = blurBitmap.height
        val cx = width / 2f
        val cy = height / 2f
        val radius = fromToBy(maxRadius, minRadius, pressure)
        blurCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        blurCanvas.drawCircle(cx, cy, maxRadius, paint)
        val blurRadius = fromToBy(shadowRadius, glowRadius, pressure)
        paintBlur.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
        val glowing = blurBitmap.extractAlpha(paintBlur, offset)
        val dx = offset.first().toFloat()
        val dy = offset.last() + shadowRadius * (1f - pressure) / 2f
        glowingPaint.color = calcBlurColor()
        canvas.drawBitmap(glowing, dx, dy, glowingPaint)
        canvas.drawCircle(cx, cy, radius, paint)

        val scale = radius / maxRadius
        canvas.translate(cx, cy)
        canvas.scale(scale, scale)
        icon.draw(canvas)
    }

    private fun calcBlurColor(): Int {
        val glowAlpha = (FULL * pressure).toInt()
        val foreground = ColorUtils.setAlphaComponent(glowColor, glowAlpha)
        return ColorUtils.compositeColors(foreground, shadowColor)
    }

    private fun fromToBy(from: Float, to: Float, by: Float) = from + (to - from) * by

    private fun MotionEvent.outside(): Boolean {
        val dx = (x - width / 2)
        val dy = (y - height / 2)
        return dx.pow(2) + dy.pow(2) > maxRadius.pow(2)
    }

    private fun press() {
        trackTouches = true
        performHapticEffect(composition.haptic.effect(press = true))
        play(RELEASED, PRESSED)
        pressure = 1f
        invalidate()
    }

    private fun release() {
        trackTouches = false
        performHapticEffect(composition.haptic.effect(press = false))
        play(PRESSED, RELEASED)
    }

    private fun play(from: Float, to: Float) {
        animator.cancel()
        animator.setFloatValues(from, to)
        animator.start()
    }
}