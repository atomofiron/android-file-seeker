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
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.extension.corner

class DockItemDrawable private constructor(
    ripple: ColorStateList,
    layers: Drawable,
    private val content: Drawable,
    private val focused: Drawable,
    private val hovered: Drawable,
    mask: Drawable,
) : RippleDrawable(ripple, layers, mask) {
    companion object {
        operator fun invoke(ripple: Int, corners: Float): DockItemDrawable {
            val content = ShapeDrawable(DockItemShape(corners))
            val focused = ShapeDrawable(DockItemShape(corners))
            val hovered = ShapeDrawable(DockItemShape(corners))
            val layers = LayerDrawable(arrayOf(
                StateListDrawable().apply { addState(intArrayOf(0), content) },
                StateListDrawable().apply { addState(intArrayOf(android.R.attr.state_hovered), hovered) },
                StateListDrawable().apply { addState(intArrayOf(android.R.attr.state_focused), focused) },
            ))
            val mask = ShapeDrawable(DockItemShape(corners))
            return DockItemDrawable(ColorStateList.valueOf(ripple), layers, content, focused, hovered, mask)
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
        val list = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_selected), intArrayOf(0)),
            intArrayOf(colors.selected, colors.default),
        )
        content.setTintList(list)
        focused.setTint(colors.focused)
        hovered.setTint(colors.hovered)
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

private class DockItemShape(private val radius: Float) : RectShape() {

    private val path = Path()

    override fun onResize(width: Float, height: Float) {
        super.onResize(width, height)
        path.reset()
        path.corner(0f, 0f, left = true, top = true, clockWise = true, radius)
        path.corner(width, 0f, left = false, top = true, clockWise = true, radius)
        path.corner(width, height, left = false, top = false, clockWise = true, radius)
        path.corner(0f, height, left = true, top = false, clockWise = true, radius)
        path.close()
    }

    override fun draw(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        canvas.drawPath(path, paint)
    }

    override fun getOutline(outline: Outline) = when {
        Android.R -> outline.setPath(path)
        else -> outline.setConvexPath(path)
    }
}