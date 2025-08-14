package app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Path.Direction
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import android.view.View.MeasureSpec
import androidx.core.view.iterator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.ExplorerStickyTopView
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerAdapter
import app.atomofiron.searchboxapp.utils.ExplorerUtils.isSeparator
import kotlin.math.max
import kotlin.math.min

class ItemBorderDecorator(
    context: Context,
    private val adapter: ExplorerAdapter,
    private val requestUpdateHeaderPosition: () -> Unit,
) : ItemDecoration() {

    private val stickyView = ExplorerStickyTopView(context)
    // примерный размер жестового навбара, чтобы игнорировать равный ему паддинг снизу
    private val gestureBar = context.resources.displayMetrics.density * 32 // 24

    private val items get() = adapter.items
    private val cornerRadius = context.resources.getDimension(R.dimen.explorer_border_corner_radius)
    private val borderWidth = context.resources.getDimension(R.dimen.explorer_border_width)
    private val listPaddingTop = context.resources.getDimension(R.dimen.padding_half)
    // под открытой не пустой директорией
    private val space = context.resources.getDimension(R.dimen.explorer_item_space)
    // под последним айтемом глубочайшей директории
    private val doubleSpace = cornerRadius * 2
    // под открытой пустой директорией
    private val tripleSpace = cornerRadius * 2.5f
    // расстояние между низом последнего айтема глубочайшей директории и нижним краем рамки
    private val frameBottomOffset = doubleSpace / 2 + borderWidth / 2

    private var deepestDir: Node? = null
    private val paint = Paint()
    private val rect = RectF()
    private val framePath = Path()

    init {
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = borderWidth
        paint.color = context.findColorByAttr(MaterialAttr.colorSecondary)
    }

    fun setDeepestDir(item: Node?) {
        deepestDir = item
        stickyView.bind(item)
        val wrapContent = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        stickyView.measure(wrapContent, wrapContent)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (view.id != R.id.item_explorer && view.id != R.id.item_explorer_separator) {
            return
        }
        val holder = parent.getChildViewHolder(view)
        val item = items[holder.bindingAdapterPosition]
        val next = items.getOrNull(holder.bindingAdapterPosition.inc())
        outRect.bottom = when {
            item.isOpened && item.isEmpty == true -> tripleSpace
            item.isOpened -> space
            item.isSeparator() -> space
            next == null -> space
            item.parentPath != next.parentPath && item.parentPath == deepestDir?.path -> doubleSpace
            item.parentPath != next.parentPath -> space
            else -> return
        }.toInt()
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val first = parent.getFirstItemView()
        first ?: return

        val firstItemViewHolder = first.first
        val itemChildCount = first.second

        rect.left = parent.paddingLeft.toFloat()
        rect.right = parent.width - parent.paddingRight.toFloat()

        var frameRect: RectF? = null
        val headerBottom = stickyView.measuredHeight + parent.paddingTop - listPaddingTop
        val paddingBottom = parent.paddingBottom.let { if (it < gestureBar) 0 else it }
        val parentBottom = (parent.height - paddingBottom).toFloat()

        val firstIndex = firstItemViewHolder.bindingAdapterPosition
        val lastIndex = firstIndex + itemChildCount.dec()
        var currentIndex = firstIndex
        for (child in parent) {
            if (child.id != R.id.item_explorer) continue
            val prev = if (currentIndex == firstIndex) null else items[currentIndex.dec()]
            val item = items[currentIndex]
            val next = if (currentIndex == lastIndex) null else items[currentIndex.inc()]
            when {
                // под открытой пустой папкой всё просто
                item.isOpened && item.isEmpty == true -> {
                    frameRect = rect
                    rect.top = child.bottom.toFloat()
                    rect.bottom = child.bottom + doubleSpace
                }
                // под глубочайшей открытой директорией задаём с рассчётом на то,
                // что дочерние айтемы может быть не видно
                item.isOpened && item.path == deepestDir?.path -> {
                    frameRect = rect
                    rect.top = child.bottom.toFloat()
                    rect.bottom = child.bottom + frameBottomOffset
                }
                item.parentPath == deepestDir?.path -> {
                    frameRect = rect
                    // верхняя граница рамки или у низа хедера текущей директории,
                    // или у низа айтема текущей директории
                    if (item.parentPath != prev?.parentPath) {
                        rect.top = child.top - space
                        rect.top = max(rect.top, headerBottom)
                    }
                    // top: хедер уезжает вместе с низом последнего айтема текущей директории
                    // bottom: указываем на нижнюю границу рамки,
                    // которая не должна быть ниже области видимости,
                    // но только если айтем текущей директории не оказывается слишком низко,
                    // чтобы игнорировать область видимости
                    if (item.parentPath != next?.parentPath) {
                        rect.top = min(rect.top, child.bottom + space)
                        rect.bottom = child.bottom + frameBottomOffset
                        rect.bottom = min(rect.bottom, parentBottom)
                        rect.bottom = max(rect.bottom, rect.top)
                    }
                }
            }
            currentIndex++
        }
        frameRect?.drawFrame(canvas)
        requestUpdateHeaderPosition()
    }

    /** @return the first item view and node item count */
    private fun RecyclerView.getFirstItemView(): Pair<ViewHolder, Int>? {
        var holder: ViewHolder? = null
        var count = 0
        for (i in 0 until childCount) {
            val view = getChildAt(i)
            if (view.id == R.id.item_explorer) {
                count++
                if (holder == null) {
                    holder = getChildViewHolder(view)
                    if (holder.bindingAdapterPosition < 0) holder = null
                }
            }
        }
        return holder?.let { it to count }
    }

    private fun RectF.drawFrame(canvas: Canvas) {
        val stroke = borderWidth
        val innerRadius = cornerRadius - stroke
        val diameter = cornerRadius * 2
        framePath.reset()
        framePath.moveTo(left + cornerRadius, top)
        framePath.rLineTo(-cornerRadius, -cornerRadius)
        framePath.arcTo(left, bottom - diameter, left + diameter, bottom, 180f, -90f, false)
        framePath.arcTo(right - diameter, bottom - diameter, right, bottom, 90f, -90f, false)
        framePath.lineTo(right, top - cornerRadius)
        framePath.rLineTo(-cornerRadius, cornerRadius)
        bottom -= stroke
        val negative = min(0f, height() / 2 - innerRadius)
        left += stroke - negative
        right -= stroke - negative
        framePath.addRoundRect(this, innerRadius, innerRadius, Direction.CW)
        canvas.drawPath(framePath, paint)
    }
}