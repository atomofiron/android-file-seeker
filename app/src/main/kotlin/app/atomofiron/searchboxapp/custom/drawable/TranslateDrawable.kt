package app.atomofiron.searchboxapp.custom.drawable

import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import androidx.core.graphics.withTranslation

fun Drawable.translated() = TranslateDrawable(this)

class TranslateDrawable(private val drawable: Drawable) : Drawable(), Drawable.Callback {

    private var dx = 0f
    private var dy = 0f

    init {
        drawable.callback = this
    }

    fun set(dx: Float = this.dx, dy: Float = this.dy) {
        if (this.dx != dx || this.dy != dy) {
            this.dx = dx
            this.dy = dy
            invalidateSelf()
        }
    }

    override fun draw(canvas: Canvas) {
        if (isVisible) canvas.withTranslation(dx, dy) {
            drawable.draw(this)
        }
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        drawable.setBounds(left, top, right, bottom)
    }

    override fun getDirtyBounds(): Rect = drawable.dirtyBounds

    override fun getHotspotBounds(outRect: Rect) = drawable.getHotspotBounds(outRect)

    override fun getIntrinsicWidth(): Int = drawable.intrinsicWidth

    override fun getIntrinsicHeight(): Int = drawable.intrinsicHeight

    override fun getState(): IntArray = drawable.state

    override fun setState(stateSet: IntArray): Boolean = drawable.setState(stateSet)

    override fun getAlpha(): Int = drawable.alpha

    override fun setAlpha(alpha: Int) = drawable.setAlpha(alpha)

    override fun getColorFilter(): ColorFilter? = drawable.colorFilter

    override fun setColorFilter(colorFilter: ColorFilter?) = drawable.setColorFilter(colorFilter)

    override fun setTintList(tint: ColorStateList?) = drawable.setTintList(tint)

    @RequiresApi(VERSION_CODES.Q)
    override fun setTintBlendMode(blendMode: BlendMode?) = drawable.setTintBlendMode(blendMode)

    override fun setVisible(visible: Boolean, restart: Boolean): Boolean {
        drawable.setVisible(visible, restart)
        return super.setVisible(visible, restart)
    }

    override fun isStateful(): Boolean = drawable.isStateful

    @RequiresApi(VERSION_CODES.S)
    override fun hasFocusStateSpecified(): Boolean = drawable.hasFocusStateSpecified()

    override fun invalidateDrawable(who: Drawable) {
        callback?.invalidateDrawable(this)
    }

    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {
        callback?.scheduleDrawable(this, what, `when`)
    }

    override fun unscheduleDrawable(who: Drawable, what: Runnable) {
        callback?.unscheduleDrawable(this, what)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun mutate(): Drawable = drawable.mutate()
}