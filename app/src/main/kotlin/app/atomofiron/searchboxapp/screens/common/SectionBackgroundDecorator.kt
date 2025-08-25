package app.atomofiron.searchboxapp.screens.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.view.children
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.extension.debugRequire
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.reflect.KClass

class SectionBackgroundDecorator(
    context: Context,
    private val groups: List<List<KClass<*>>>,
) : RecyclerView.ItemDecoration() {

    private val paint = Paint()
    private val marginHalf = context.resources.getDimensionPixelSize(R.dimen.padding_half)
    private val internalRadius = context.resources.getDimension(R.dimen.corner_nano)
    private val edgeRadius = context.resources.getDimension(R.dimen.corner_extra)
    private val internalSpace = internalRadius / 2

    init {
        paint.style = Paint.Style.FILL
        paint.color = context.findColorByAttr(MaterialAttr.colorSurfaceContainer)
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
        val left = parent.paddingLeft.toFloat()
        val width = parent.run { width - paddingStart - paddingEnd }.toFloat()
        val halfSpace = internalSpace / 2
        var from = if (reversed) last else first
        if (hasPrev) from -= halfSpace.roundToInt()
        var to = if (reversed) first else last
        if (hasNext) to += halfSpace.roundToInt()
        val offset = edgeRadius * 2 * (to - from).sign
        var radius = if (hasPrev) internalRadius else edgeRadius
        val bottom = if (hasPrev) from + offset else to.toFloat()
        canvas.drawRoundRect(left, from.toFloat(), left + width, bottom, radius, radius, paint)
        radius = if (hasNext) internalRadius else edgeRadius
        val top = if (hasNext) to - offset else from.toFloat()
        canvas.drawRoundRect(left, top, left + width, to.toFloat(), radius, radius, paint)
    }

    private fun List<List<KClass<*>>>.findGroup(item: Any?): List<KClass<*>>? {
        item ?: return null
        return find { it.contains(item::class) }
    }
}