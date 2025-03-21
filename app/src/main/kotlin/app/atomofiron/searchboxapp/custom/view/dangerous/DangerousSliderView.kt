package app.atomofiron.searchboxapp.custom.view.dangerous

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.CharacterStyle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.MaterialDimen
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.utils.disallowInterceptTouches
import app.atomofiron.searchboxapp.utils.isRtl
import app.atomofiron.searchboxapp.utils.toIntAlpha
import com.google.android.material.color.MaterialColors
import com.google.android.material.textview.MaterialTextView
import kotlin.math.PI
import kotlin.math.sin

private const val OFFSET_DURATION = 512L
private const val TIP_DURATION = 3072L
private const val BOUNCE_DURATION = 1024L
private const val START = 0f
private const val END = 1f
private val HapticRange = 0.1f..0.9f
private const val Pi = PI.toFloat()

class DangerousSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr), ValueAnimator.AnimatorUpdateListener {

    private val strokeWidth = resources.getDimensionPixelSize(MaterialDimen.m3_comp_outlined_button_outline_width)
    private val textColor = MaterialColors.getColor(context, MaterialAttr.colorOnError, 0)
    private val trackColor = MaterialColors.getColor(context, android.R.attr.colorBackground, 0)
    private val thumbColor = MaterialColors.getColor(context, MaterialAttr.colorError, 0)
    private val tipColor = MaterialColors.getColor(context, MaterialAttr.colorOnErrorContainer, 0)
    private val done = ContextCompat.getDrawable(context, R.drawable.ic_done)!!

    private val thumb = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, thumbColor.let { intArrayOf(it, it) })
    private val button = MaterialTextView(context)
    private val tip = MaterialTextView(context)
    private val thumbSpan = DangerousThumbSpan(button, thumbColor, textColor)
    private val tipSpan = DangerousTipSpan(tip)
    private val arrows = DangerousArrows(thumbColor, strokeWidth.toFloat())
    private var border = GradientDrawable()

    private val isRtl = isRtl()
    private var downX: Float? = null
    private var direction = if (isRtl) -1 else 1
    private val tipMinOffset get() = (button.width - button.height / 3) / 2f
    private val tipOffset get() = (tipMinOffset + tipMinOffset * progress) * direction
    private var offset
        get() = button.translationX
        set(value) { button.translationX = value }
    private val maxOffset get() = (width - button.width).toFloat() * direction
    private val maxBounceOffset get() = button.width / 2f
    private val progress get() = if (maxOffset == 0f) START else offset / maxOffset
    private var hapticAllowed = false
    private val isDone get() = !done.bounds.isEmpty
    var listener: (() -> Boolean)? = null

    private val offsetAnimator = ValueAnimator.ofFloat(0f)
    private val tipAnimator = ValueAnimator.ofFloat(START, END)
    private val bounceAnimator = ValueAnimator.ofFloat(0f)
    private val bounceInterpolator = BounceInterpolator()
    private val clipping = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) = when {
            isRtl -> outline.setRoundRect(0, 0, view.right - view.left + offset.toInt(), view.bottom - view.top, view.height / 2f)
            else -> outline.setRoundRect(offset.toInt(), 0, view.right - view.left, view.bottom - view.top, view.height / 2f)
        }
    }

    init {
        setWillNotDraw(false)
        val padding = resources.getDimensionPixelSize(MaterialDimen.m3_btn_padding_left)
        setPadding(padding, padding / 2, padding, padding / 2)

        thumb.cornerRadius = Float.MAX_VALUE
        thumb.setStroke(strokeWidth, thumbColor)
        border.cornerRadius = Float.MAX_VALUE
        border.setStroke(strokeWidth, thumbColor)
        done.setTint(textColor)

        val styled = context.obtainStyledAttributes(attrs, R.styleable.DangerousSliderView, defStyleAttr, 0)
        tip.text = styled.getString(R.styleable.DangerousSliderView_tip)?.withSpan(tipSpan)
        button.text = styled.getString(R.styleable.DangerousSliderView_text)?.withSpan(thumbSpan)
        val textSize = styled.getDimension(R.styleable.DangerousSliderView_textSize, button.textSize)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
        styled.recycle()

        tip.setTextColor(tipColor)
        tip.layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { gravity = Gravity.CENTER }
        button.layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        button.background = thumb
        addView(tip)
        addView(button)

        offsetAnimator.addUpdateListener(this)
        offsetAnimator.interpolator = DecelerateInterpolator()
        tipAnimator.addUpdateListener(this)
        tipAnimator.interpolator = LinearInterpolator()
        tipAnimator.duration = TIP_DURATION
        bounceAnimator.addUpdateListener(this)
        bounceAnimator.interpolator = LinearInterpolator()
        bounceAnimator.duration = BOUNCE_DURATION
        outlineProvider = clipping
        clipToOutline = true
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        button.setPadding(left, top, right, bottom)
        tip.setPadding(left, top, right, bottom)
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        button.setPaddingRelative(start, top, end, bottom)
        tip.setPaddingRelative(start, top, end, bottom)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        arrows.setBounds(0, 0, width, height)
        border.setBounds(0, 0, width, height)
        updateOffset(0f)
    }

    override fun draw(canvas: Canvas) {
        val arrowsAlpha = tipAnimator.animatedValue as Float * 2 - 1
        arrows.draw(canvas, flip = isRtl, progress = progress, alpha = arrowsAlpha, offset = tipOffset, arrowSize = tip.textSize / 2)
        border.draw(canvas)
        super.draw(canvas)
        if (isDone) done.draw(canvas)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        downX = when (event.action) {
            MotionEvent.ACTION_DOWN -> when {
                isDone -> null
                event.x < button.run { left + offset } -> null
                event.x > button.run { right + offset } -> null
                else -> {
                    offsetAnimator.cancel()
                    bounceAnimator.cancel()
                    parent.disallowInterceptTouches()
                    event.x
                }
            }
            MotionEvent.ACTION_MOVE -> downX?.let {
                updateOffset(offset + (event.x - it))
                event.x
            }
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> when {
                progress == END && listener?.invoke() == true -> onDone()
                else -> onRelease()
            }.let { null }
            else -> downX
        }
        return downX != null
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val value = animation.animatedValue as Float
        if (animation === offsetAnimator) {
            updateOffset(value)
        } else if (animation === tipAnimator) {
            tipSpan.progress = value
            invalidate()
        } else if (animation === bounceAnimator) {
            val scale = when {
                value < END -> sin(value * Pi / 2)
                else -> 1 - bounceInterpolator.getInterpolation((value - END) / END)
            }
            updateOffset(maxBounceOffset * scale)
        }
    }

    fun setText(@StringRes resId: Int) = setText(resources.getString(resId))

    fun setText(text: CharSequence?) = button.setText(text?.withSpan(thumbSpan))

    fun setTip(@StringRes resId: Int) = setTip(resources.getString(resId))

    fun setTip(text: CharSequence?) = tip.setText(text?.withSpan(tipSpan))

    fun setTextSize(unit: Int, size: Float) {
        button.setTextSize(unit, size)
        tip.setTextSize(unit, size)
    }

    private fun onRelease() {
        if (offset == 0f) {
            bounceAnimator.setFloatValues(START, END * 2)
            bounceAnimator.start()
        } else {
            offsetAnimator.setFloatValues(offset, 0f)
            offsetAnimator.duration = (progress * OFFSET_DURATION).toLong()
            offsetAnimator.start()
        }
        when {
            progress == END -> Unit
            tipAnimator.isRunning -> Unit
            else -> tipAnimator.start()
        }
    }

    private fun onDone() {
        val left = button.left + button.width / 2 - done.intrinsicWidth / 2 + offset.toInt()
        val top = button.top + button.height / 2 - done.intrinsicHeight / 2
        done.setBounds(left, top, left + done.intrinsicWidth, top + done.intrinsicHeight)
        button.setTextColor(Color.TRANSPARENT)
        invalidate()
    }

    private fun updateOffset(translation: Float) {
        offset = translation.inRange(0f, maxOffset)
        thumbSpan.progress = progress
        tip.translationX = tipOffset
        val alpha = progress.toIntAlpha()
        var thumbColor = ColorUtils.setAlphaComponent(thumbColor, alpha)
        thumbColor = ColorUtils.compositeColors(thumbColor, trackColor)
        thumb.setColor(thumbColor)
        border.alpha = alpha
        if (hapticAllowed && (progress == END || progress == START)) {
            button.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            hapticAllowed = false
        }
        hapticAllowed = hapticAllowed || (progress in HapticRange && !bounceAnimator.isRunning)
        invalidate()
        invalidateOutline()
    }

    private fun CharSequence.withSpan(span: CharacterStyle): Spannable {
        val string = SpannableString(this)
        string.setSpan(span, 0, string.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return string
    }

    private fun Float.inRange(first: Float, second: Float): Float = when {
        first < second -> when {
            this < first -> first
            this > second -> second
            else -> this
        }
        first > second -> when {
            this < second -> second
            this > first -> first
            else -> this
        }
        else -> first
    }
}
