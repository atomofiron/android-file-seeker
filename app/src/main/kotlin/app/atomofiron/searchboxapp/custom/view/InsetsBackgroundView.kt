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
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.getColorByAttr
import lib.atomofiron.insets.ExtendedWindowInsets
import lib.atomofiron.insets.ExtendedWindowInsets.Type
import lib.atomofiron.insets.InsetsListener
import lib.atomofiron.insets.TypeSet
import lib.atomofiron.insets.attachInsetsListener
import kotlin.math.max

private fun Context.getSystemBarsColor(): Int {
    val color = getColorByAttr(R.attr.colorBackground)
    return ColorUtils.setAlphaComponent(color, Alpha.LEVEL_67)
}

class InsetsBackgroundView : View, InsetsListener {
    private companion object {
        const val BOTTOM =     0b001
        const val HORIZONTAL = 0b010
        const val ALL =        0b011
    }

    @JvmInline
    private value class Sides(val value: Int) {
        val horizontal: Boolean get() = (value and HORIZONTAL != 0)
        val bottom: Boolean get() = (value and BOTTOM != 0)
        val empty: Boolean get() = (value and ALL == 0)
    }

    override var types = Type.statusBars + Type.navigationBars
        private set
    private var transparent = TypeSet.Empty

    private var leftInset = 0
    private var topInset = 0
    private var rightInset = 0
    private var bottomInset = 0
    private var statusBarTop = 0

    private val paint = Paint()

    private var statusBar = true
    private var sides = Sides(ALL)

    private val maxStatusBar = resources.getDimensionPixelSize(R.dimen.status_bar_max)
    private val statusBarMinPadding = resources.getDimensionPixelSize(R.dimen.status_bar_min_padding)
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

    fun transparent(types: TypeSet) {
        transparent = types
    }

    override fun onApplyWindowInsets(windowInsets: ExtendedWindowInsets) {
        navigationBar = windowInsets[Type.navigationBars]
        cutout = windowInsets[Type.displayCutout]
        val statusBars = windowInsets[Type.statusBars]
        val common = windowInsets[types]
        val navigation = windowInsets[Type.navigationBars]
        val tappable = windowInsets[Type.tappableElement]
        val transparent = windowInsets[transparent]
        leftInset = resolve(common.left, navigation.left, tappable.left, transparent.left)
        topInset = common.top
        rightInset = resolve(common.right, navigation.right, tappable.right, transparent.right)
        bottomInset = resolve(common.bottom, navigation.bottom, tappable.bottom, transparent.bottom)
        statusBarTop = statusBars.top
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!statusBar && sides.empty) return

        val leftInset = leftInset.toFloat()
        val rightInset = rightInset.toFloat()
        val bottomInset = bottomInset.toFloat()

        val width = width.toFloat()
        val height = height.toFloat()

        val navigationHorizontalTop = when {
            statusBar -> canvas.drawStatusBar()
            else -> 0f
        }
        sides.takeIf { !it.empty }?.run {
            if (horizontal) {
                canvas.drawRect(0f, navigationHorizontalTop, leftInset, height - bottomInset, paint)
                canvas.drawRect(width - rightInset, navigationHorizontalTop, width, height - bottomInset, paint)
            }
            if (bottom) canvas.drawRect(0f, height - bottomInset, width, height, paint)
        }
    }

    private fun Canvas.drawStatusBar(): Float {
        calcStatusBarPadding(statusBarTop, maxStatusBar, statusBarMinPadding, cutout, navigationBar, topLeftCorner, topRightCorner) { left, vertical, right ->
            val bottom = when (topInset) {
                statusBarTop -> statusBarTop - vertical
                else -> topInset
            }
            val radius = statusBarTop / 2f - vertical
            drawRoundRect(left.toFloat(), vertical.toFloat(), width - right.toFloat(), bottom.toFloat(), radius, radius, paint)
            return 0f
        }
        drawRect(0f, 0f, width.toFloat(), topInset.toFloat(), paint)
        return topInset.toFloat()
    }

    private fun resolve(target: Int, navigation: Int, tappable: Int, transparent: Int): Int = when {
        transparent > target -> 0 // something is greater than target
        target != navigation -> target
        target == tappable -> target
        else -> 0 // gesture navigation bar
    }
}

fun View.calcStatusBarPadding(insets: ExtendedWindowInsets): Insets {
    val statusBar = insets[ExtType.statusBars].top
    val cutout = insets[ExtType.displayCutout]
    val navigationBar = insets[ExtType.navigationBars]
    val maxStatusBar = resources.getDimensionPixelSize(R.dimen.status_bar_max)
    val statusBarMinPadding = resources.getDimensionPixelSize(R.dimen.status_bar_min_padding)
    val topLeftCorner = if (Android.S) rootWindowInsets?.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT)
        ?.radius ?: 0 else 0
    val topRightCorner = if (Android.S) rootWindowInsets?.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT)
        ?.radius ?: 0 else 0
    calcStatusBarPadding(statusBar, maxStatusBar, statusBarMinPadding, cutout, navigationBar, topLeftCorner, topRightCorner) { left, vertical, right ->
        return Insets.of(left, vertical, right, vertical)
    }
    return Insets.NONE
}

private inline fun calcStatusBarPadding(
    statusBar: Int,
    maxStatusBar: Int,
    statusBarMinPadding: Int,
    cutout: Insets,
    navigationBar: Insets,
    topLeftCorner: Int,
    topRightCorner: Int,
    action: (left: Int, vertical: Int, right: Int) -> Unit,
): Boolean {
    val padding = (statusBar - maxStatusBar) / 2
    return if (padding <= statusBarMinPadding) { // <= !!
        false
    } else {
        val cutout = max(cutout.left, cutout.right)
        val marginLeft = max(topLeftCorner * 0.6f - cutout, 0f).toInt()
        val marginRight = max(topRightCorner * 0.6f - cutout, 0f).toInt()
        val rawLeft = max(navigationBar.left, cutout)
        val rawRight = max(navigationBar.right, cutout)
        val left = rawLeft + max(marginLeft, padding)
        val right = rawRight + max(marginRight, padding)
        action(left, padding, right)
        true
    }
}
