package app.atomofiron.searchboxapp.custom.view.dangerous

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM
import android.text.Spannable
import android.text.SpannableString
import android.text.style.CharacterStyle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.MaterialDimen
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.utils.disallowInterceptTouches
import app.atomofiron.searchboxapp.utils.isRtl
import app.atomofiron.searchboxapp.utils.over
import app.atomofiron.searchboxapp.utils.performHapticLite
import app.atomofiron.searchboxapp.utils.toIntAlpha
import app.atomofiron.searchboxapp.utils.withAlpha
import com.google.android.material.color.MaterialColors
import com.google.android.material.textview.MaterialTextView
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

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
    defStyleRes: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), ValueAnimator.AnimatorUpdateListener {

    private val defaultPadding = resources.getDimensionPixelSize(MaterialDimen.m3_btn_padding_left)
    private val strokeWidth = resources.getDimensionPixelSize(MaterialDimen.m3_comp_outlined_button_outline_width)
    private val textColor = MaterialColors.getColor(context, MaterialAttr.colorOnError, 0)
    private var thumbColor = MaterialColors.getColor(context, MaterialAttr.colorSurfaceContainer, 0)
    private var startBorderColor = thumbColor
    private var endBorderColor = MaterialColors.getColor(context, MaterialAttr.colorError, 0)
    private val tipColor = MaterialColors.getColor(context, MaterialAttr.colorOnErrorContainer, 0)
    private val done = ContextCompat.getDrawable(context, R.drawable.ic_done)!!

    private val thumb by dependOn(::endBorderColor) { GradientDrawable(TOP_BOTTOM, intArrayOf(it, it)) }
    private val button = MaterialTextView(context)
    private var icon: Drawable? = null
    private val tip = MaterialTextView(context)
    private val thumbSpan by dependOn(::endBorderColor) { DangerousThumbSpan(button, it, textColor) }
    private val tipSpan = DangerousTipSpan(tip)
    private val arrows by dependOn(::endBorderColor) { DangerousArrows(it, strokeWidth.toFloat()) }
    private var borderPath = Path()
    private var borderPaint = Paint()
    private var cornerRadius = Float.MAX_VALUE

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
    private var initialized = false

    private val offsetAnimator = ValueAnimator.ofFloat(0f)
    private val tipAnimator = ValueAnimator.ofFloat(START, END)
    private val bounceAnimator = ValueAnimator.ofFloat(0f)
    private val bounceInterpolator = BounceInterpolator()

    init {
        setWillNotDraw(false)
        when (paddingStart + paddingTop + paddingEnd + paddingBottom) {
            0 -> setPaddingRelative(defaultPadding, defaultPadding / 2, defaultPadding, defaultPadding / 2)
            else -> setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom)
        }
        super.setPaddingRelative(0, 0, 0, 0)

        context.withStyledAttributes(attrs, R.styleable.DangerousSliderView, defStyleAttr, 0) {
            tip.text = getString(R.styleable.DangerousSliderView_tip)?.withSpan(tipSpan)
            button.text = getString(R.styleable.DangerousSliderView_text)?.withSpan(thumbSpan)
            val textSize = getDimension(R.styleable.DangerousSliderView_textSize, button.textSize)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            cornerRadius = getDimension(R.styleable.DangerousSliderView_cornerRadius, cornerRadius)
            thumb.cornerRadius = cornerRadius
            thumbColor = getColor(R.styleable.DangerousSliderView_thumbColor, thumbColor)
            startBorderColor = getColor(R.styleable.DangerousSliderView_startBorderColor, startBorderColor)
            endBorderColor = getColor(R.styleable.DangerousSliderView_endBorderColor, endBorderColor)
            val thumbBorder = getBoolean(R.styleable.DangerousSliderView_thumbBorder, false)
            setThumbBorder(thumbBorder)
            icon = getDrawable(R.styleable.DangerousSliderView_icon)
            button.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
            button.compoundDrawablePadding = getDimensionPixelSize(R.styleable.DangerousSliderView_iconPadding, button.paddingStart)
        }

        done.setTint(textColor)
        borderPaint.isAntiAlias = true
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = strokeWidth.toFloat()

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
        initialized = true
    }

    fun setStartBorder(color: Int) {
        startBorderColor = color
    }

    fun setEndBorder(color: Int) {
        endBorderColor = color
    }

    fun setThumbBorder(visible: Boolean) {
        val color = if (visible) endBorderColor else Color.TRANSPARENT
        thumb.setStroke(strokeWidth, color)
    }

    fun setThumbColor(color: Int) {
        thumbColor = color
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) = button.setPadding(left, top, right, bottom)

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) = button.setPaddingRelative(start, top, end, bottom)

    override fun getPaddingStart(): Int = if (initialized) button.paddingStart else super.getPaddingStart()

    override fun getPaddingTop(): Int = if (initialized) button.paddingTop else super.getPaddingTop()

    override fun getPaddingEnd(): Int = if (initialized) button.paddingEnd else super.getPaddingEnd()

    override fun getPaddingBottom(): Int = if (initialized) button.paddingBottom else super.getPaddingBottom()

    override fun getPaddingLeft(): Int = if (initialized) button.paddingLeft else super.getPaddingLeft()

    override fun getPaddingRight(): Int = if (initialized) button.paddingRight else super.getPaddingRight()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        arrows.setBounds(0, 0, width, height)
        updateOffset(0f)
    }

    override fun draw(canvas: Canvas) {
        val arrowsAlpha = tipAnimator.animatedValue as Float * 2 - 1
        arrows.draw(canvas, flip = isRtl, progress = progress, alpha = arrowsAlpha, offset = tipOffset, arrowSize = tip.textSize / 2)
        canvas.drawPath(borderPath, borderPaint)
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
        icon?.run {
            val alpha = (max(0f, progress - 0.5f) / 0.5f)
            setTint(textColor withAlpha alpha over endBorderColor)
        }
        tip.translationX = tipOffset
        val alpha = progress.toIntAlpha()
        thumb.setColor(endBorderColor withAlpha alpha over thumbColor)
        val inset = strokeWidth / 2f
        val borderInsetLeft = inset + if (isRtl) 0f else translation
        val borderInsetRight = inset + if (isRtl) -translation else 0f
        borderPath.reset()
        borderPath.addRoundRect(borderInsetLeft, inset, width - borderInsetRight, height - inset, cornerRadius, cornerRadius, Path.Direction.CW)
        borderPaint.color = endBorderColor withAlpha alpha over startBorderColor
        if (hapticAllowed && (progress == END || progress == START)) {
            button.performHapticLite()
            hapticAllowed = false
        }
        hapticAllowed = hapticAllowed || (progress in HapticRange && !bounceAnimator.isRunning)
        invalidate()
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

fun <D,T> dependOn(dependency: () -> D, factory: (D) -> T) = object : ReadOnlyProperty<Any?, T> {

    private var dependencyValue = dependency()
    private var value = factory(dependencyValue)

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val dep = dependency()
        if (dep != dependencyValue) {
            dependencyValue = dep
            value = factory(dep)
        }
        return value
    }
}
