package app.atomofiron.searchboxapp.screens.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.view.View
import android.view.ViewOutlineProvider
import androidx.core.graphics.withTranslation
import androidx.core.view.children
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceGroupAdapter
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.Android
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.drawable.colorSurfaceContainer
import app.atomofiron.searchboxapp.utils.Size
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class SettingsBackgroundDecorator(
    context: Context,
    private val mergedKeys: List<List<String>> = emptyList(),
) : RecyclerView.ItemDecoration() {

    private val paint = Paint()
    private val paddingHalf = context.resources.getDimensionPixelSize(R.dimen.padding_half)
    private val edgeRadius = context.resources.getDimension(R.dimen.corner_extra)
    private val gap = context.resources.getDimension(R.dimen.padding_micro)

    init {
        paint.style = Paint.Style.FILL
        paint.color = context.colorSurfaceContainer()
        paint.isAntiAlias = true
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val preference = parent.preferenceOf(view) ?: return
        val group = preference.parent ?: return
        val previous = parent.preferenceOf(view, -1)
        val next = parent.preferenceOf(view, 1)
        val first = previous?.parent !== group
        val last = next?.parent !== group
        if (view.outlineProvider !is OutlineProvider) {
            view.outlineProvider = OutlineProvider()
            view.clipToOutline = true
        }
        outRect.set(
            paddingHalf,
            offsetFor(first, preference.isMergedWith(previous)),
            paddingHalf,
            offsetFor(last, preference.isMergedWith(next)),
        )
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        for (child in parent.children) {
            val item = child.tag as? Item ?: Item()
            item.preference = parent.preferenceOf(child)
            child.tag = item
        }
        for (index in 0 until parent.childCount) {
            val child = parent.getChildAt(index)
            val item = child.tag as? Item ?: continue
            val group = item.group ?: continue
            val previousChild = parent.getChildAt(index.dec())
            val nextChild = parent.getChildAt(index.inc())
            val previous = (previousChild?.tag as? Item)?.preference
            val next = (nextChild?.tag as? Item)?.preference
            val first = previous?.parent !== group
            val last = next?.parent !== group
            val topDistance = distanceBetween(previousChild, child)
            val bottomDistance = distanceBetween(child, nextChild)
            canvas.draw(
                child = child,
                topRadius = radiiFor(first, topDistance),
                bottomRadius = radiiFor(last, bottomDistance),
            )
        }
    }

    private fun offsetFor(edge: Boolean, merged: Boolean): Int = when {
        edge || merged -> 0
        else -> (gap / 2).roundToInt()
    }

    private fun radiiFor(edge: Boolean, distance: Float): Float = when {
        edge -> edgeRadius
        else -> max(min(distance, edgeRadius), 0f)
    }

    private fun distanceBetween(above: View?, below: View?): Float = when {
        above == null || below == null -> edgeRadius
        else -> below.y - above.y - above.height
    }

    private fun Preference.isMergedWith(other: Preference?): Boolean {
        if (other == null || other.parent !== parent) {
            return false
        }
        val key = key ?: return false
        val otherKey = other.key ?: return false
        return mergedKeys.any { key in it && otherKey in it }
    }

    @SuppressLint("RestrictedApi")
    private fun RecyclerView.preferenceOf(view: View, step: Int = 0): Preference? {
        val adapter = adapter as? PreferenceGroupAdapter ?: return null
        val position = getChildAdapterPosition(view) + step
        if (position !in 0 until adapter.itemCount) {
            return null
        }
        val preference = adapter.getItem(position) ?: return null
        return when {
            !preference.isVisible -> null
            preference is PreferenceGroup -> null
            else -> preference
        }
    }

    private fun Canvas.draw(child: View, topRadius: Float, bottomRadius: Float) {
        val outline = child.outlineProvider as OutlineProvider
        outline.updateRadii(child, topRadius, bottomRadius)
        withTranslation(child.left.toFloat(), child.y) {
            drawPath(outline.path, paint)
        }
    }

    private class Item {
        var preference: Preference? = null
        val group: PreferenceGroup? get() = preference?.parent
    }

    private class OutlineProvider : ViewOutlineProvider() {

        val radii = FloatArray(8)
        val path = Path()

        private var size = Size()

        @Suppress("DEPRECATION")
        override fun getOutline(view: View, outline: Outline) = when {
            Android.R -> outline.setPath(path)
            else -> outline.setConvexPath(path)
        }

        fun updateRadii(view: View, top: Float, bottom: Float) {
            val new = Size(view.width, view.height)
            val changed = radii[0] != top || radii[4] != bottom || new != size
            for (i in radii.indices) {
                radii[i] = if (i < 4) top else bottom
            }
            if (changed) {
                size = new
                path.reset()
                path.addRoundRect(0f, 0f, view.width.toFloat(), view.height.toFloat(), radii, Path.Direction.CW)
            }
        }
    }
}
