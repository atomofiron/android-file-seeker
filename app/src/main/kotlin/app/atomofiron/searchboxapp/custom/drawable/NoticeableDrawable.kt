package app.atomofiron.searchboxapp.custom.drawable

import android.R.attr.state_activated
import android.R.attr.state_enabled
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.utils.Const

class NoticeableDrawable(
    drawable: Drawable,
    private var dotColor: Int,
    private val overrideAlpha: Boolean = true,
) : LayerDrawable(arrayOf(drawable)) {
    private var clipOutPath = Path()
    private val circlePath = Path()
    private val paint = Paint()

    private var drawDot = false
    private var forceDrawDot = false

    private val dotRadius: Float get() = bounds.width().toFloat() / 6
    private val holeRadius: Float get() = bounds.width().toFloat() / 4
    private var dotAlpha = Const.ALPHA_VISIBLE_INT
    private val holeX: Float get() = bounds.right - dotRadius
    private val holeY: Float get() = dotRadius

    init {
        paint.isAntiAlias = true
    }

    constructor(
        context: Context,
        @DrawableRes iconId: Int,
        @ColorRes dotColorId: Int = R.color.red,
        overrideAlpha: Boolean = true,
    ) : this(context, ContextCompat.getDrawable(context, iconId)!!, dotColorId, overrideAlpha)

    constructor(
        context: Context,
        icon: Drawable,
        @ColorRes dotColorId: Int = R.color.red,
        overrideAlpha: Boolean = true,
    ) : this(icon, ContextCompat.getColor(context, dotColorId), overrideAlpha)

    fun setDotColor(color: Int) {
        dotColor = color
        invalidateSelf()
    }

    fun forceShowDot(show: Boolean) {
        forceDrawDot = show
        invalidateSelf()
    }

    override fun onStateChange(state: IntArray): Boolean {
        drawDot = state.contains(state_activated)
        val isEnabled = state.contains(state_enabled)
        dotAlpha = when {
            isEnabled -> Const.ALPHA_VISIBLE_INT
            else -> Const.ALPHA_DISABLED_INT
        }
        if (overrideAlpha) {
            alpha = dotAlpha
        }
        invalidateSelf()
        return super.onStateChange(state)
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)

        circlePath.reset()
        circlePath.addCircle(holeX, holeY, holeRadius, Path.Direction.CW)
        circlePath.close()
        clipOutPath.reset()
        clipOutPath.addRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), Path.Direction.CW)
        clipOutPath.op(circlePath, Path.Op.DIFFERENCE)
        clipOutPath.close()
    }

    override fun draw(canvas: Canvas) = when {
        drawDot || forceDrawDot -> canvas.run {
            paint.color = dotColor
            paint.alpha = dotAlpha
            drawCircle(holeX, holeY, dotRadius, paint)

            clipPath(clipOutPath)
            super.draw(this)
        }
        else -> super.draw(canvas)
    }
}