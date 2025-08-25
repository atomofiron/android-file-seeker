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
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.reflect.KClass

class SectionBackgroundDecorator(
    context: Context,
    private val groups: List<List<KClass<*>>>,
) : RecyclerView.ItemDecoration() {

    private val paint = Paint()
    private val internalRadius = context.resources.getDimension(R.dimen.corner_nano)
    private val edgeRadius = context.resources.getDimension(R.dimen.corner_extra)
    private val internalSpace = internalRadius / 2

    init {
        paint.style = Paint.Style.FILL
        paint.color = context.findColorByAttr(MaterialAttr.colorSurfaceContainer)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) = Unit

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val adapter = parent.adapter as? ListAdapter<*,*> ?: return
        val layoutManager = parent.layoutManager ?: return
        var range: IntRange? = null
        var current: List<KClass<*>>? = null
        var prev: List<KClass<*>>? = null
        for (child in parent.children) {
            val holder = parent.getChildViewHolder(child)
            val item = adapter.takeIf { holder.bindingAdapterPosition >= 0 }
                ?.currentList
                ?.get(holder.bindingAdapterPosition)
                ?: continue
            val group = groups.find { it.contains(item::class) }
            if (group == null || current != null && group !== current) {
                range?.draw(canvas, parent, hasPrev = prev != null, hasNext = group != null, layoutManager.isLayoutReversed)
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
        range?.draw(canvas, parent, hasPrev = prev != null, hasNext = false, layoutManager.isLayoutReversed)
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
        val from = if (reversed) last else first
        val to = if (reversed) first else last
        val halfOffset = edgeRadius * (to - from).sign
        val firstHalf = (from + to) / 2 + halfOffset
        val lastHalf = (from + to) / 2 - halfOffset
        var radius = if (hasPrev) internalRadius else edgeRadius
        var space = if (hasPrev) halfSpace else 0f
        canvas.drawRoundRect(left, from - space, left + width, firstHalf, radius, radius, paint)
        radius = if (hasNext) internalRadius else edgeRadius
        space = if (hasNext) halfSpace else 0f
        canvas.drawRoundRect(left, lastHalf, left + width, to + space, radius, radius, paint)
    }
}