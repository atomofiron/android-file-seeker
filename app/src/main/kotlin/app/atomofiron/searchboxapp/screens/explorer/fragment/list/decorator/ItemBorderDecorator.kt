package app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Path.Direction
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import androidx.core.view.children
import androidx.core.view.iterator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerAdapter
import app.atomofiron.searchboxapp.utils.ExplorerUtils.isSeparator
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.colorAttr
import lib.atomofiron.insets.ExtendedWindowInsets
import lib.atomofiron.insets.InsetsListener
import kotlin.math.max
import kotlin.math.min

class ItemBorderDecorator(
    context: Context,
    private val adapter: ExplorerAdapter,
    private val deepestStickyProvider: () -> View?,
) : ItemDecoration(), InsetsListener {

    private var deepestStickyView: View? = null
    // примерный размер жестового навбара, чтобы игнорировать равный ему паддинг снизу
    private val gestureBar = context.resources.displayMetrics.density * 32 // 24

    private val items get() = adapter.items
    private val cornerRadius = context.resources.getDimension(R.dimen.explorer_border_corner_radius)
    private val borderWidth = context.resources.getDimension(R.dimen.explorer_border_width)
    // под открытой не пустой директорией
    private val space = context.resources.getDimension(R.dimen.explorer_item_space)
    // под последним айтемом глубочайшей директории
    private val doubleSpace = space * 2
    // под открытой пустой директорией
    private val tripleSpace = space * 2.5f
    // расстояние между низом последнего айтема глубочайшей директории и нижним краем рамки
    private val frameBottomOffset = doubleSpace / 2 + borderWidth / 2

    private var deepestDir: Node? = null
    private val paint = Paint()
    private val rect = RectF()
    private val framePath = Path()
    private var ignoreBottom = false

    init {
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = borderWidth
        paint.color = context.colorAttr(MaterialAttr.colorSecondary)
    }

    fun setDeepestDir(item: Node?) {
        deepestDir = item
        deepestStickyView = deepestStickyProvider()
    }

    override fun onApplyWindowInsets(windowInsets: ExtendedWindowInsets) {
        val navigation = windowInsets[ExtType.navigationBars].bottom
        val tappable = windowInsets[ExtType.tappableElement].bottom
        val gestures = windowInsets[ExtType.systemGestures].bottom
        val dock = windowInsets[ExtType.dock].bottom
        ignoreBottom = dock == 0 && navigation == gestures && navigation != tappable // only gesture navigation bar
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (view.id != R.id.item_explorer && view.id != R.id.item_explorer_separator) {
            return
        }
        val holder = parent.getChildViewHolder(view)
        val item = items[holder.bindingAdapterPosition]
        val next = items.getOrNull(holder.bindingAdapterPosition.inc())
        if (holder.bindingAdapterPosition == 0) {
            outRect.top = space.toInt()
        }
        outRect.bottom = when {
            item.isOpenedAndEmpty(next) -> tripleSpace
            item.isOpened -> space
            item.isSeparator() -> space
            next == null -> space
            item.parentRef != next.parentRef && item.parentRef == deepestDir?.ref -> doubleSpace
            item.parentRef != next.parentRef -> space
            else -> return
        }.toInt()
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val first = parent.getFirstItemView()
        first ?: return
        val stickyView = deepestStickyView ?: parent.children.find {
            val holder = parent.findContainingViewHolder(it)
            holder ?: return@find false
            val item = adapter.getOrNull(holder.bindingAdapterPosition)
            item?.isDeepest == true // stickyView is null because of deepest item is empty (without children) that never gets stick
        } ?: return

        val firstItemViewHolder = first.first
        val itemChildCount = first.second

        rect.left = parent.paddingLeft.toFloat()
        rect.right = parent.width - parent.paddingRight.toFloat()

        var frameRect: RectF? = null
        val headerBottom = stickyView.measuredHeight + parent.paddingTop
        val paddingBottom = parent.paddingBottom.let { if (it < gestureBar) 0 else it }
        val parentBottom = parent.height - if (ignoreBottom) 0f else paddingBottom.toFloat()

        val firstIndex = firstItemViewHolder.bindingAdapterPosition
        val lastIndex = firstIndex + itemChildCount.dec()
        var currentIndex = firstIndex
        var prevChildBottom = 0f
        for (child in parent) {
            if (child.id != R.id.item_explorer) continue
            val prev = if (currentIndex == firstIndex) null else items[currentIndex.dec()]
            val item = items[currentIndex]
            val next = if (currentIndex == lastIndex) null else items[currentIndex.inc()]
            val childBottom = child.trueBottom()
            when {
                // под открытой пустой папкой всё просто
                item.isOpenedAndEmpty(next) -> {
                    frameRect = rect
                    rect.top = childBottom
                    rect.bottom = childBottom + doubleSpace
                }
                // под глубочайшей открытой директорией задаём с рассчётом на то,
                // что дочерние айтемы может быть не видно
                item.isOpened && item.ref == deepestDir?.ref -> {
                    frameRect = rect
                    rect.top = childBottom
                    rect.bottom = childBottom + frameBottomOffset
                }
                item.parentRef == deepestDir?.ref -> {
                    frameRect = rect
                    // верхняя граница рамки или у низа хедера текущей директории,
                    // или у низа айтема текущей директории
                    if (item.parentRef != prev?.parentRef) {
                        rect.top = prevChildBottom
                        rect.top = max(rect.top, headerBottom.toFloat())
                    }
                    // top: хедер уезжает вместе с низом последнего айтема текущей директории
                    // bottom: указываем на нижнюю границу рамки,
                    // которая не должна быть ниже области видимости,
                    // но только если айтем текущей директории не оказывается слишком низко,
                    // чтобы игнорировать область видимости
                    if (item.parentRef != next?.parentRef) {
                        rect.top = min(rect.top, childBottom + space)
                        rect.bottom = childBottom + frameBottomOffset
                        rect.bottom = min(rect.bottom, parentBottom)
                        rect.bottom = max(rect.bottom, rect.top)
                    }
                }
            }
            currentIndex++
            prevChildBottom = childBottom
        }
        frameRect?.drawFrame(canvas)
    }

    private fun View.trueBottom() = y + height

    /** @return the first item view and node item count */
    private fun RecyclerView.getFirstItemView(): Pair<ViewHolder, Int>? {
        var holder: ViewHolder? = null
        var count = 0
        for (view in this) {
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
        val arm = cornerRadius / 2
        framePath.reset()
        framePath.moveTo(left + cornerRadius, top)
        framePath.rCubicTo(-arm, 0f, -cornerRadius, -cornerRadius + arm, -cornerRadius, -cornerRadius)
        framePath.arcTo(left, bottom - diameter, left + diameter, bottom, 180f, -90f, false)
        framePath.arcTo(right - diameter, bottom - diameter, right, bottom, 90f, -90f, false)
        framePath.lineTo(right, top - cornerRadius)
        framePath.rCubicTo(0f, arm, -cornerRadius + arm, cornerRadius, -cornerRadius, cornerRadius)
        bottom -= stroke
        val negative = min(0f, height() / 2 - innerRadius)
        left += stroke - negative
        right -= stroke - negative
        framePath.addRoundRect(this, innerRadius, innerRadius, Direction.CW)
        canvas.drawPath(framePath, paint)
    }

    private fun Node.isOpenedAndEmpty(next: Node?) = isOpened && (next == null || ref.isChildOf(next.parentRef)) && !isSeparator()
}