package app.atomofiron.searchboxapp.custom.view.dock.item

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.StateListDrawable
import android.graphics.drawable.shapes.RectShape
import androidx.core.graphics.Insets
import androidx.core.graphics.alpha
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.extension.corner
import app.atomofiron.searchboxapp.utils.inverseColor
import app.atomofiron.searchboxapp.utils.withAlpha

class DockItemDrawable private constructor(
    ripple: ColorStateList,
    layers: Drawable,
    private val content: Drawable,
    private val focused: Drawable,
    private val hovered: Drawable,
    mask: Drawable,
) : RippleDrawable(ripple, layers, mask) {
    companion object {
        operator fun invoke(ripple: Int, primary: Int, surface: Int, stroke: Float, corners: Float): DockItemDrawable {
            val content = ShapeDrawable(DockItemShape(corners))
            val focused = ShapeDrawable(DockItemShape(corners, inset = stroke / 2))
            focused.paint.style = Paint.Style.STROKE
            focused.paint.strokeWidth = stroke
            val hovered = ShapeDrawable(DockItemShape(corners))
            val layers = LayerDrawable(
                arrayOf(
                    StateListDrawable().apply { addState(intArrayOf(0), content) },
                    StateListDrawable().apply { addState(intArrayOf(android.R.attr.state_hovered), hovered) },
                    StateListDrawable().apply { addState(intArrayOf(android.R.attr.state_focused), focused) },
                )
            )
            val mask = ShapeDrawable(DockItemShape(corners))
            val rippleList = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_activated), intArrayOf(0)),
                intArrayOf(surface withAlpha ripple.alpha, ripple),
            )
            return DockItemDrawable(rippleList, layers, content, focused, hovered, mask)
        }
    }

    private var insets = Insets.NONE
    private var srcBounds = Rect()

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        if (!srcBounds.equals(left, top, right, bottom)) {
            srcBounds.set(left, top, right, bottom)
            setBounds()
        }
    }

    fun setColors(colors: DockItemColors) {
        ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_activated), intArrayOf(android.R.attr.state_selected), intArrayOf()),
            intArrayOf(colors.activated, colors.selected, colors.default),
        ).let { content.setTintList(it) }
        ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_activated), intArrayOf()),
            intArrayOf(colors.focused.inverseColor(), colors.focused),
        ).let { focused.setTintList(it) }
        ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_activated), intArrayOf()),
            intArrayOf(colors.hovered.inverseColor(), colors.hovered),
        ).let { hovered.setTintList(it) }
    }

    fun setInsets(insets: Insets) {
        if (insets != this.insets) {
            this.insets = insets
            setBounds()
        }
    }

    private fun setBounds() = srcBounds.run { super.setBounds(left + insets.left, top + insets.top, right - insets.right, bottom - insets.bottom) }

    private fun Rect.equals(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        return this.left == left && this.top == top && this.right == right && this.bottom == bottom
    }
}

private class DockItemShape(
    private val radius: Float,
    private val inset: Float = 0f,
) : RectShape() {

    private val path = Path()

    override fun onResize(width: Float, height: Float) {
        super.onResize(width, height)
        path.reset()
        path.corner(inset, inset, left = true, top = true, clockWise = true, radius)
        path.corner(width - inset, inset, left = false, top = true, clockWise = true, radius)
        path.corner(width - inset, height - inset, left = false, top = false, clockWise = true, radius)
        path.corner(inset, height - inset, left = true, top = false, clockWise = true, radius)
        path.close()
    }

    override fun draw(canvas: Canvas, paint: Paint) = canvas.drawPath(path, paint)

    override fun getOutline(outline: Outline) = when {
        Android.R -> outline.setPath(path)
        else -> outline.setConvexPath(path)
    }
}