package app.atomofiron.searchboxapp.screens.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.FILL_ROW
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.common.util.extension.debugFail
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.drawable.colorSurfaceContainer
import app.atomofiron.searchboxapp.utils.isRtl
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.reflect.KClass

private const val RADII = 8

class SectionBackgroundDecorator(
    context: Context,
    private val groups: List<List<KClass<*>>>,
) : RecyclerView.ItemDecoration() {

    private val paint = Paint()
    private val path = Path()
    private val radii = FloatArray(RADII)
    private val paddingHalf = context.resources.getDimensionPixelSize(R.dimen.padding_half)
    private val paddingMini = context.resources.getDimension(R.dimen.padding_mini)
    private val internalRadius = context.resources.getDimension(R.dimen.corner_nano)
    private val edgeRadius = context.resources.getDimension(R.dimen.corner_extra)
    private val innerSpace = (internalRadius / 2).roundToInt()

    init {
        paint.style = Paint.Style.FILL
        paint.color = context.colorSurfaceContainer()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = parent.adapter as? ListAdapter<*,*> ?: return
        val reversed = parent.layoutManager?.isLayoutReversed ?: return
        val holder = parent.findContainingViewHolder(view) ?: return
        holder as GeneralHolder<*>
        var position = holder.bindingAdapterPosition
        if (position < 0) {
            debugFail { position }
            return
        }
        val item = adapter.currentList[position]
        val current = groups.findGroup(item)
        var viewIndex = parent.children.indexOfLast { it === view }.dec()
        position--
        val minWidth = holder.minWidth().let {
            if (it == FILL_ROW) it else it + holder.itemView.run { marginStart + marginEnd }
        }
        var free = parent.run { width - paddingStart - paddingEnd } - minWidth
        if (current != null && minWidth != FILL_ROW && parent.getFree(viewIndex) > minWidth) {
            while (free > 0) {
                val other = parent.getChildAt(--viewIndex)
                val prevItem = adapter.currentList.getOrNull(--position)
                val prev = groups.findGroup(prevItem)
                when {
                    other == null -> break
                    prev != current -> break
                    else -> free -= other.run { width + marginStart + marginEnd }
                }
            }
        }
        if (position < 0) return
        val prevItem = adapter.currentList.getOrNull(position)
        val prev = groups.findGroup(prevItem)
        val offset = when {
            current == null && prev == null -> 0
            current == prev -> 0
            current != null && prev != null -> paddingHalf + innerSpace
            else -> paddingMini.roundToInt()
        }
        when {
            reversed -> outRect.bottom = offset
            else -> outRect.top = offset
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
        val left = parent.paddingLeft - paddingMini
        val right = parent.run { width - paddingRight } + paddingMini
        val top = start - paddingMini
        val bottom = last + paddingMini
        val topRadius = if (hasTop) internalRadius else edgeRadius
        radii.setTopRadius(topRadius)
        val bottomRadius = if (hasBottom) internalRadius else edgeRadius
        radii.setBottomRadius(bottomRadius)
        path.reset()
        path.addRoundRect(left, top, right, bottom, radii, Path.Direction.CW)
        canvas.drawPath(path, paint)
    }

    private fun ViewGroup.getFree(childIndex: Int): Int {
        val child = getChildAt(childIndex)
        return when {
            child == null -> 0
            isRtl() -> child.left - child.marginLeft - paddingLeft
            else -> width - paddingRight - child.right - child.marginRight
        }
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