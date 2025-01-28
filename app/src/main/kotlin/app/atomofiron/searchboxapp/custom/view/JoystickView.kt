package app.atomofiron.searchboxapp.custom.view

import android.animation.ValueAnimator
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
import app.atomofiron.common.util.findBooleanByAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.MaterialAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.preference.JoystickComposition
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

class JoystickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    companion object {
        private const val GLOW_DURATION = 256L
        private const val BLUR_RADIUS_DP = 4f
    }
    private val icon = ContextCompat.getDrawable(context, R.drawable.ic_esc)!!
    private val iconSize = resources.getDimensionPixelSize(R.dimen.icon_size)
    private val padding = resources.getDimensionPixelSize(R.dimen.padding_large)

    private val paintBlur = Paint()
    private val glowingPaint = Paint()
    private val paint = Paint()

    private var trackTouchEvent = false

    private val density = resources.displayMetrics.density
    private val maxBlurRadius = BLUR_RADIUS_DP * density
    private var brightness = 0f
    private val glowAnimator = ValueAnimator.ofFloat(0f, (Math.PI / 2).toFloat())

    private var composition: JoystickComposition? = null
    private var bitmap: Bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8)

    init {
        paint.isAntiAlias = true
        glowAnimator.duration = GLOW_DURATION
        glowAnimator.addUpdateListener { animator ->
            val value = animator.animatedValue as Float
            brightness = 1f - sin(value.toDouble()).toFloat()
            invalidate()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val size = min(right - left, bottom - top)
        when {
            size == 0 -> return
            bitmap.width == size -> Unit
            else -> {
                bitmap.recycle()
                bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            }
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
        glowingPaint.color = when {
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                glowAnimator.cancel()
                trackTouchEvent = true
                brightness = 1f
                invalidate()
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS)
            }
            MotionEvent.ACTION_MOVE -> {
                if (trackTouchEvent) {
                    val contains = event.x > 0f && event.y > 0f &&
                            event.x.toInt() < width && event.y.toInt() < height

                    if (!contains) {
                        trackTouchEvent = false
                        glowAnimator.start()
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_RELEASE)
                    }
                }
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                if (trackTouchEvent) {
                    glowAnimator.start()
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_RELEASE)
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val width = bitmap.width
        val height = bitmap.width
        val cx = (width / 2).toFloat()
        val cy = (height / 2).toFloat()
        val blurRadius = maxBlurRadius * brightness
        val radius = min(width, height) / 2 - blurRadius / 2 - padding

        if (brightness != 0f) {
            val mCanvas = Canvas(bitmap)
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            mCanvas.drawCircle(cx, cy, radius, paint)
            paintBlur.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
            val glowing = bitmap.extractAlpha(paintBlur, IntArray(2))
            val left = (width - glowing.width).toFloat() / 2
            val top = (height - glowing.height).toFloat() / 2
            canvas.drawBitmap(glowing, left, top, glowingPaint)
        }
        canvas.drawCircle(cx, cy, radius, paint)

        val offset = blurRadius / 2
        val half = iconSize / 2
        val left = (cx - half + offset).roundToInt()
        val top = (cy - half + offset).roundToInt()
        val right = (cx + half - offset).roundToInt()
        val bottom = (cy + half - offset).roundToInt()
        icon.setBounds(left, top, right, bottom)
        icon.draw(canvas)
    }
}