package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.ColorUtils
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.getColorByAttr
import app.atomofiron.searchboxapp.utils.obtainStyledAttributes
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

    override val types get() = common + Type.navigationBars

    private var leftInset = 0
    private var topInset = 0
    private var rightInset = 0
    private var bottomInset = 0

    private val paint = Paint()

    private var common = Type.statusBars
        set(value) { field = value + Type.statusBars }
    private var statusBar = true
    private var sides = Sides(ALL)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        paint.color = context.getSystemBarsColor()

        context.obtainStyledAttributes(attrs, R.styleable.SystemUiBackgroundView, defStyleAttr) {
            statusBar = getBoolean(R.styleable.SystemUiBackgroundView_statusBar, statusBar)
            sides = getInt(R.styleable.SystemUiBackgroundView_navigationBar, sides.value).let {
                Sides(it)
            }
        }
        attachInsetsListener(this)
    }

    fun setColor(color: Int, alpha: Int = Alpha.LEVEL_67) {
        paint.color = ColorUtils.setAlphaComponent(color, alpha)
    }

    fun setAdditional(types: TypeSet) {
        common = types
    }

    override fun onApplyWindowInsets(windowInsets: ExtendedWindowInsets) {
        val common = windowInsets[common]
        val navigationBars = windowInsets[Type.navigationBars]
        val tappableElement = windowInsets[Type.tappableElement]
        leftInset = max(common.left, navigationBars.left.only(tappableElement.left))
        topInset = common.top
        rightInset = max(common.right, navigationBars.right.only(tappableElement.right))
        bottomInset = max(common.bottom, navigationBars.bottom.only(tappableElement.bottom))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!statusBar && sides.empty) return

        val leftInset = leftInset.toFloat()
        val topInset = topInset.toFloat()
        val rightInset = rightInset.toFloat()
        val bottomInset = bottomInset.toFloat()

        val width = width.toFloat()
        val height = height.toFloat()

        if (statusBar) canvas.drawRect(0f, 0f, width, topInset, paint)

        sides.takeIf { !it.empty }?.run {
            val navigationTop = if (statusBar) topInset else 0f
            if (horizontal) {
                canvas.drawRect(0f, navigationTop, leftInset, height - bottomInset, paint)
                canvas.drawRect(width - rightInset, navigationTop, width, height - bottomInset, paint)
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