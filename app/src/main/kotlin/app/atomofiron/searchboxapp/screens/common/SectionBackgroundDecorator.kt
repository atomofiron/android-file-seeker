package app.atomofiron.searchboxapp.screens.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.view.View
import androidx.core.view.children
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.extension.debugRequire
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.drawable.colorSurfaceContainer
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

private const val RADII = 8

class SectionBackgroundDecorator(
    context: Context,
    private val groups: List<List<KClass<*>>>,
) : RecyclerView.ItemDecoration() {

    private val paint = Paint()
    private val path = Path()
    private val radii = FloatArray(RADII)
    private val marginHalf = context.resources.getDimensionPixelSize(R.dimen.padding_half)
    private val internalRadius = context.resources.getDimension(R.dimen.corner_nano)
    private val edgeRadius = context.resources.getDimension(R.dimen.corner_extra)
    private val internalSpaceHalf = internalRadius / 4

    init {
        paint.style = Paint.Style.FILL
        paint.color = context.colorSurfaceContainer()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = parent.adapter as? ListAdapter<*,*> ?: return
        val reversed = parent.layoutManager?.isLayoutReversed ?: return
        val holder = parent.findContainingViewHolder(view) ?: return
        debugRequire(holder.bindingAdapterPosition >= 0)
        if (holder.bindingAdapterPosition < 0) {
            return
        }
        val item = adapter.currentList[holder.bindingAdapterPosition]
        val nextItem = adapter.currentList.getOrNull(holder.bindingAdapterPosition.inc())
        val prevItem = adapter.currentList.getOrNull(holder.bindingAdapterPosition.dec())
        val current = groups.findGroup(item)
        if (current != null) {
            // we cant set top/bottom to the multiple items in a group, so we set to neighbours
            return
        }
        val next = groups.findGroup(nextItem)
        val prev = groups.findGroup(prevItem)
        when {
            next == null -> Unit
            reversed -> outRect.top = marginHalf
            else -> outRect.bottom = marginHalf
        }
        when {
            prev == null -> Unit
            reversed -> outRect.bottom = marginHalf
            else -> outRect.top = marginHalf
        }
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = parent.adapter as? ListAdapter<*,*> ?: return
        val reversed = parent.layoutManager?.isLayoutReversed ?: return
        var range: IntRange? = null
        var current: List<KClass<*>>? = null
        var prev: List<KClass<*>>? = null
        for (child in parent.children) {
            val holder = parent.getChildViewHolder(child)
            val item = adapter.takeIf { holder.bindingAdapterPosition >= 0 }
                ?.currentList
                ?.get(holder.bindingAdapterPosition)
                ?: continue
            val group = groups.findGroup(item)
            if (group == null || current != null && group !== current) {
                range?.draw(canvas, parent, hasPrev = prev != null, hasNext = group != null, reversed)
                range = null
                prev = current
            }
            current = group
            group ?: continue
            val top = child.top - child.marginTop
            val bottom = child.bottom + child.marginBottom
            range = when {
                range == null -> top..bottom
                else -> min(range.first, top)..max(range.last, bottom)
            }
        }
        range?.draw(canvas, parent, hasPrev = prev != null, hasNext = false, reversed)
    }

    private fun IntRange.draw(
        canvas: Canvas,
        parent: RecyclerView,
        hasPrev: Boolean,
        hasNext: Boolean,
        reversed: Boolean,
    ) {
        val hasTop = if (reversed) hasNext else hasPrev
        val hasBottom = if (reversed) hasPrev else hasNext
        val left = parent.paddingLeft.toFloat()
        val right = parent.run { width - paddingRight }.toFloat()
        var top = start.toFloat()
        if (hasTop) top += internalSpaceHalf
        var bottom = last.toFloat()
        if (hasBottom) bottom -= internalSpaceHalf
        val topRadius = if (hasTop) internalRadius else edgeRadius
        radii.setTopRadius(topRadius)
        val bottomRadius = if (hasBottom) internalRadius else edgeRadius
        radii.setBottomRadius(bottomRadius)
        path.reset()
        path.addRoundRect(left, top, right, bottom, radii, Path.Direction.CW)
        canvas.drawPath(path, paint)
    }

    private fun FloatArray.setTopRadius(value: Float) {
        for (i in 0..<RADII / 2) set(i, value)
    }

    private fun FloatArray.setBottomRadius(value: Float) {
        for (i in 4..<RADII) set(i, value)
    }

    private fun List<List<KClass<*>>>.findGroup(item: Any?): List<KClass<*>>? {
        item ?: return null
        return find { it.contains(item::class) }
    }
}