package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.RoundedCorner
import android.view.View
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.Insets
import app.atomofiron.common.util.Android
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.getColorByAttr
import lib.atomofiron.insets.ExtendedWindowInsets
import lib.atomofiron.insets.ExtendedWindowInsets.Type
import lib.atomofiron.insets.InsetsListener
import lib.atomofiron.insets.TypeSet
import lib.atomofiron.insets.attachInsetsListener
import kotlin.math.max

class InsetsBackgroundView : View, InsetsListener {
    companion object {
        fun Context.getSystemBarsColor(): Int {
            val color = getColorByAttr(R.attr.colorBackground)
            return ColorUtils.setAlphaComponent(color, Alpha.LEVEL_67)
        }
        private const val BOTTOM =     0b001
        private const val HORIZONTAL = 0b010
        private const val ALL =        0b011
    }

    @JvmInline
    value class Sides(val value: Int) {
        val horizontal: Boolean get() = (value and HORIZONTAL != 0)
        val bottom: Boolean get() = (value and BOTTOM != 0)
        val empty: Boolean get() = (value and ALL == 0)
    }

    override var types = Type.statusBars + Type.navigationBars
        private set

    private var leftInset = 0
    private var topInset = 0
    private var rightInset = 0
    private var bottomInset = 0
    private var statusBarTop = 0

    private val paint = Paint()

    private var statusBar = true
    private var sides = Sides(ALL)

    private val maxStatusBar: Float
    private val statusBarMinPadding: Float
    private var navigationBar = Insets.NONE
    private var cutout = Insets.NONE
    private var topLeftCorner = 0
    private var topRightCorner = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        paint.color = context.getSystemBarsColor()

        context.withStyledAttributes(attrs, R.styleable.InsetsBackgroundView, defStyleAttr) {
            statusBar = getBoolean(R.styleable.InsetsBackgroundView_statusBar, statusBar)
            sides = getInt(R.styleable.InsetsBackgroundView_navigationBar, sides.value)
                .let { Sides(it) }
        }
        maxStatusBar = resources.getDimension(R.dimen.status_bar_max)
        statusBarMinPadding = resources.getDimension(R.dimen.status_bar_min_padding)
        attachInsetsListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (Android.S) {
            topLeftCorner = rootWindowInsets?.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)
                ?.radius ?: topLeftCorner
            topRightCorner = rootWindowInsets?.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT)
                ?.radius ?: topRightCorner
        }
    }

    fun setColor(color: Int, alpha: Int = Alpha.LEVEL_67) {
        paint.color = ColorUtils.setAlphaComponent(color, alpha)
    }

    operator fun plusAssign(types: TypeSet) {
        this.types += types
    }

    override fun onApplyWindowInsets(windowInsets: ExtendedWindowInsets) {
        navigationBar = windowInsets[Type.navigationBars]
        cutout = windowInsets[Type.displayCutout]
        val statusBars = windowInsets[Type.statusBars]
        val common = windowInsets[types]
        val navigationBars = windowInsets[Type.navigationBars]
        val tappableElement = windowInsets[Type.tappableElement]
        leftInset = max(common.left, navigationBars.left.only(tappableElement.left))
        topInset = common.top
        rightInset = max(common.right, navigationBars.right.only(tappableElement.right))
        bottomInset = max(common.bottom, navigationBars.bottom.only(tappableElement.bottom))
        statusBarTop = statusBars.top
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!statusBar && sides.empty) return

        val leftInset = leftInset.toFloat()
        val topInset = topInset.toFloat()
        val rightInset = rightInset.toFloat()
        val bottomInset = bottomInset.toFloat()
        val statusBarTop = statusBarTop.toFloat()

        val width = width.toFloat()
        val height = height.toFloat()

        var navigationHorizontalTop = 0f
        if (statusBar) {
            val padding = (statusBarTop - maxStatusBar) / 2
            if (padding > statusBarMinPadding) {
                val cutout = max(cutout.left, cutout.right)
                val marginLeft = max(topLeftCorner * 0.6f - cutout, 0f)
                val marginRight = max(topRightCorner * 0.6f - cutout, 0f)
                val rawLeft = max(navigationBar.left, cutout)
                val rawRight = max(navigationBar.right, cutout)
                val left = rawLeft + max(marginLeft, padding)
                val right = width - rawRight - max(marginRight, padding)
                val bottom = when (topInset) {
                    statusBarTop -> topInset - padding
                    else -> statusBarTop
                }
                canvas.drawRoundRect(left, padding, right, bottom, maxStatusBar, maxStatusBar, paint)
            } else {
                canvas.drawRect(0f, 0f, width, topInset, paint)
                navigationHorizontalTop = topInset
            }
        }
        sides.takeIf { !it.empty }?.run {
            if (horizontal) {
                canvas.drawRect(0f, navigationHorizontalTop, leftInset, height - bottomInset, paint)
                canvas.drawRect(width - rightInset, navigationHorizontalTop, width, height - bottomInset, paint)
            }
            if (bottom) canvas.drawRect(0f, height - bottomInset, width, height, paint)
        }
    }

    fun update(
        statusBar: Boolean = this.statusBar,
        sides: Sides = this.sides,
    ) {
        this.statusBar = statusBar
        this.sides = sides
        invalidate()
    }

    private fun Int.only(value: Int): Int = if (this == value) this else 0
}