package app.atomofiron.searchboxapp.custom.drawable

import android.R.attr.state_activated
import android.R.attr.state_enabled
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build.VERSION_CODES.Q
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.toIntAlpha

class NoticeableDrawable(
    private val drawable: Drawable,
    private var dotColor: Int,
    private val overrideAlpha: Boolean = false,
) : Drawable(), Drawable.Callback {
    private var clipOutPath = Path()
    private val circlePath = Path()
    private val paint = Paint()

    private var drawDot = false
    private var forceDrawDot = false

    private val dotRadius: Float get() = bounds.width().toFloat() / 6
    private val holeRadius: Float get() = bounds.width().toFloat() / 4
    private var dotAlpha = Alpha.VISIBLE_INT
    private val holeX: Float get() = bounds.right - dotRadius
    private val holeY: Float get() = dotRadius

    init {
        paint.isAntiAlias = true
    }

    constructor(
        context: Context,
        @DrawableRes iconId: Int,
        @ColorRes dotColorId: Int = R.color.red,
        overrideAlpha: Boolean = false,
    ) : this(context, ContextCompat.getDrawable(context, iconId)!!, dotColorId, overrideAlpha)

    constructor(
        context: Context,
        icon: Drawable,
        @ColorRes dotColorId: Int = R.color.red,
        overrideAlpha: Boolean = false,
    ) : this(icon, ContextCompat.getColor(context, dotColorId), overrideAlpha)

    fun setDotColor(color: Int): NoticeableDrawable {
        dotColor = color
        invalidateSelf()
        return this
    }

    fun forceShowDot(show: Boolean): NoticeableDrawable {
        forceDrawDot = show
        invalidateSelf()
        return this
    }

    override fun getIntrinsicWidth(): Int = drawable.intrinsicWidth

    override fun getIntrinsicHeight(): Int = drawable.intrinsicHeight

    override fun getMinimumWidth(): Int = drawable.minimumWidth

    override fun getMinimumHeight(): Int = drawable.minimumHeight

    override fun getAlpha(): Int = drawable.alpha

    override fun onStateChange(state: IntArray): Boolean {
        drawDot = state.contains(state_activated).also {
            if (it != drawDot) invalidateSelf()
        }
        val isEnabled = !state.contains(-state_enabled)
        dotAlpha = Alpha.enabled(isEnabled).toIntAlpha()
        if (overrideAlpha) {
            alpha = dotAlpha
        }
        return super.onStateChange(state)
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        drawable.setBounds(left, top, right, bottom)

        circlePath.reset()
        circlePath.addCircle(holeX, holeY, holeRadius, Path.Direction.CW)
        circlePath.close()
        clipOutPath.reset()
        clipOutPath.addRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), Path.Direction.CW)
        clipOutPath.op(circlePath, Path.Op.DIFFERENCE)
        clipOutPath.close()
    }

    override fun draw(canvas: Canvas)  {
        if (!isVisible) {
            return
        }
        if (drawDot || forceDrawDot) {
            paint.color = dotColor
            paint.alpha = dotAlpha
            canvas.drawCircle(holeX, holeY, dotRadius, paint)
            canvas.clipPath(clipOutPath)
        }
        drawable.draw(canvas)
    }

    override fun setState(stateSet: IntArray): Boolean {
        super.setState(stateSet)
        return drawable.setState(stateSet)
    }

    override fun isStateful(): Boolean = drawable.isStateful()

    override fun setAlpha(alpha: Int) = drawable.setAlpha(alpha)

    override fun setColorFilter(colorFilter: ColorFilter?) = drawable.setColorFilter(colorFilter)

    override fun setTintList(tint: ColorStateList?) = drawable.setTintList(tint)

    override fun setTint(tintColor: Int) = drawable.setTint(tintColor)

    override fun setTintMode(tintMode: PorterDuff.Mode?) = drawable.setTintMode(tintMode)

    @RequiresApi(Q)
    override fun setTintBlendMode(blendMode: BlendMode?) = drawable.setTintBlendMode(blendMode)

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun setColorFilter(color: Int, mode: PorterDuff.Mode) = drawable.setColorFilter(color, mode)

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = drawable.opacity

    override fun invalidateDrawable(who: Drawable) {
        callback?.invalidateDrawable(this)
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        callback?.scheduleDrawable(this, what, `when`)
    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        callback?.unscheduleDrawable(this, what)
    }
}