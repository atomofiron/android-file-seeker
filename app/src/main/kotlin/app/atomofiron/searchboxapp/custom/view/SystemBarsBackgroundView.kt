package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.ColorUtils
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.utils.getColorByAttr
import app.atomofiron.searchboxapp.utils.obtainStyledAttributes
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.isLayoutRtl
import lib.atomofiron.insets.ExtendedWindowInsets
import lib.atomofiron.insets.ExtendedWindowInsets.Type
import lib.atomofiron.insets.InsetsListener
import lib.atomofiron.insets.attachInsetsListener
import kotlin.math.max

class SystemBarsBackgroundView : View, InsetsListener {
    companion object {
        fun Context.getSystemBarsColor(): Int {
            val color = getColorByAttr(R.attr.colorBackground)
            return ColorUtils.setAlphaComponent(color, Const.ALPHA_67_PERCENT)
        }
        private const val START =  0b00001
        private const val TOP =    0b00010
        private const val END =    0b00100
        private const val BOTTOM = 0b01000
        private const val ALL =    0b01111
        private const val RTL =    0b10000
    }

    @JvmInline
    value class Sides(val value: Int) {
        val left: Boolean get() = if (value and RTL == 0) (value and START != 0) else (value and END != 0)
        val top: Boolean get() = (value and TOP != 0)
        val right: Boolean get() = if (value and RTL == 0) (value and START != 0) else (value and END != 0)
        val bottom: Boolean get() = (value and BOTTOM != 0)
        val empty: Boolean get() = (value == 0)

        constructor(value: Int, rtl: Boolean) : this(value + if(rtl) RTL else 0)
    }

    override val types = Type.systemBars

    private var leftInset = 0
    private var topInset = 0
    private var rightInset = 0
    private var bottomInset = 0

    private val paint = Paint()

    private var statusBar = true
    private var navigationBar = Sides(ALL)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        paint.color = context.getSystemBarsColor()

        context.obtainStyledAttributes(attrs, R.styleable.SystemUiBackgroundView, defStyleAttr) {
            statusBar = getBoolean(R.styleable.SystemUiBackgroundView_statusBar, statusBar)
            navigationBar = getInt(R.styleable.SystemUiBackgroundView_navigationBar, navigationBar.value).let {
                Sides(it, isLayoutRtl)
            }
        }
        attachInsetsListener(this)
    }

    override fun onApplyWindowInsets(windowInsets: ExtendedWindowInsets) {
        val statusBars = windowInsets[Type.statusBars]
        val navigationBars = windowInsets[Type.navigationBars]
        val tappableElement = windowInsets[Type.tappableElement]
        leftInset = max(statusBars.left, navigationBars.left.only(tappableElement.left))
        topInset = statusBars.top
        rightInset = max(statusBars.right, navigationBars.right.only(tappableElement.right))
        bottomInset = max(statusBars.bottom, navigationBars.bottom.only(tappableElement.bottom))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!statusBar && navigationBar.empty) return

        val leftInset = leftInset.toFloat()
        val topInset = topInset.toFloat()
        val rightInset = rightInset.toFloat()
        val bottomInset = bottomInset.toFloat()

        val width = width.toFloat()
        val height = height.toFloat()

        if (statusBar) canvas.drawRect(0f, 0f, width, topInset, paint)

        navigationBar.takeIf { !it.empty }?.run {
            val navigationTop = if (statusBar) topInset else 0f
            if (left) canvas.drawRect(0f, navigationTop, leftInset, height - bottomInset, paint)
            if (right) canvas.drawRect(width - rightInset, navigationTop, width, height - bottomInset, paint)
            if (bottom) canvas.drawRect(0f, height - bottomInset, width, height, paint)
        }
    }

    fun update(
        statusBar: Boolean = this.statusBar,
        navigationBar: Sides = this.navigationBar,
    ) {
        this.statusBar = statusBar
        this.navigationBar = navigationBar
        invalidate()
    }

    private fun Int.only(value: Int): Int = if (this == value) this else 0
}