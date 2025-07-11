package app.atomofiron.common.recycler

import android.content.res.Resources
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.extension.ceilToInt
import app.atomofiron.common.util.extension.clear
import app.atomofiron.fileseeker.R
import java.util.LinkedList

private const val COLUMNS = 144u
private const val COLUMNS_INT = 144

private typealias Holder = GeneralHolder<*>

private data class Cell(
    // these should be stable
    val width: Int,
    val type: Int,
    val hungry: Boolean,
    // these are volatile
    val columns: UInt,
    val rowId: Int,
)

interface AdapterHolderListener {
    fun onCreate(holder: Holder, viewType: Int)
    fun onBind(holder: Holder, position: Int)
}

class FinderSpanSizeLookup(
    private val adapter: ListAdapter<out GeneralItem, *>,
    private val manager: GridLayoutManager,
    resources: Resources,
)  : GridLayoutManager.SpanSizeLookup()
    , AdapterHolderListener
    , View.OnLayoutChangeListener
{

    private val portraitWidth = resources.getDimension(R.dimen.screen_compact)
    lateinit var recyclerView: RecyclerView
    private val itemCount get() = adapter.itemCount
    private val holders = LinkedList<Holder>()
    private val cache = hashMapOf<Long,Cell>()
    private val cells = mutableListOf<Cell>()

    private var availableArea = 0f
    private var columnWidth = 0f
    private var portraitColumns = 0u
    private val repeatTrigger = RepeatTrigger()

    init {
        manager.spanCount = COLUMNS_INT
        adapter.registerAdapterDataObserver(ItemObserver())
    }

    override fun onCreate(holder: Holder, viewType: Int) {
        if (!holders.any { it === holder }) {
            holders.add(holder)
        }
    }

    override fun onBind(holder: Holder, position: Int) {
        val minWidth = holder.minWidth().ceilToInt()
        val itemId = adapter.getItemId(position)
        val cell = cells.getOrNull(position) ?: cache[itemId]
        if (minWidth != cell?.width || position > cells.size) {
            calcSize(position, minWidth)
        }
    }

    fun onMeasure(width: Int) {
        if (availableArea == 0f) {
            updateArea(width)
        }
    }

    override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
        updateArea(right - left)
    }

    private fun updateArea(width: Int = recyclerView.width) {
        val available = width.toFloat() - recyclerView.run { paddingStart + paddingEnd }
        if (available != availableArea) {
            availableArea = available
            columnWidth = availableArea / COLUMNS_INT
            portraitColumns = (portraitWidth / columnWidth).toUInt().coerceAtLeast(1u)
            invalidateSpanGroupIndexCache()
            for (i in cells.indices) calcSize(i)
            manager.requestLayout()
        }
    }

    override fun getSpanSize(position: Int): Int = calcSize(position)

    private fun calcSize(position: Int, width: Int? = null): Int {
        if (position > cells.size) {
            calcSize(position.dec())
        }
        updateArea()
        val itemId = adapter.getItemId(position)
        val minWidth: Int
        val type: Int
        val hungry: Boolean
        val holder = holderAt(position)
        val cached = cells.getOrNull(position) ?: cache[itemId]
        if (holder != null) {
            minWidth = width ?: holder.minWidth().ceilToInt()
            type = holder.itemViewType
            hungry = holder.hungry
        } else if (cached != null) {
            // columns and rowId are volatile
            minWidth = cached.width
            type = cached.type
            hungry = cached.hungry
        } else {
            return COLUMNS_INT // otherwise ArrayIndexOutOfBoundsException: length=145; index=288 GridLayoutManager.getSpaceForSpanRange(1031)
        }
        var rowId = when (position) {
            0 -> itemId.toInt()
            else -> cells[position.dec()].rowId
        }
        val consumed = consumed(rowId, position)
        var left = COLUMNS
        when {
            consumed >= COLUMNS -> rowId = itemId.toInt()
            else -> left -= consumed
        }
        val columns = minWidth.toColumns(hungry)
        if (columns > left) {
            rowId = itemId.toInt()
            //left = COLUMNS
        }
        val cell = Cell(minWidth, type, hungry, columns, rowId)
        when {
            position < cells.size -> cells[position] = cell
            else -> cells.add(position, cell)
        }
        cache[itemId] = cell
        return when {
            !isComplete(rowId) -> columns
            availableArea <= portraitWidth && count(rowId) == 1 -> COLUMNS // consider like hungry
            else -> columns + calcFree(rowId, position)
        }.toInt()
    }

    private fun isComplete(rowId: Int): Boolean {
        require(cells.size <= itemCount)
        require(cells.isNotEmpty())
        return cells.size == itemCount || rowId != cells.last().rowId
    }

    private fun count(rowId: Int): Int = cells.count { it.rowId == rowId }

    private fun holderAt(position: Int): Holder? = holders.find { it.absoluteAdapterPosition == position }

    private fun consumed(rowId: Int, position: Int) = cells.asSequence()
        .filterIndexed { index, cell -> cell.rowId == rowId && index < position }
        .sumOf { it.columns }

    private fun calcFree(rowId: Int, position: Int): UInt {
        val row = cells.filter { it.rowId == rowId }
        val index = position - cells.indexOfFirst { it.rowId == rowId }
        val cell = row[index]
        val prevRowPosition = position.dec() - index
        val prevRowCell = cells.getOrNull(prevRowPosition)
        val hungry = when {
            cell.hungry -> row.count { it.hungry }.toUInt()
            row.size == 1 && prevRowCell?.let { it.type == cell.type } != true -> return 0u
            row.any { it.type != cell.type } -> return 0u
            // consider like hungry
            row.size == 1 -> return calcFree(prevRowCell!!.rowId, prevRowPosition)
            else -> row.size.toUInt()
        }
        val free = COLUMNS - row.sumOf { it.columns }
        val supplement = free / hungry
        return when {
            index.toUInt() < free % hungry -> supplement.inc()
            else -> supplement
        }
    }

    private fun Int.toColumns(hungry: Boolean): UInt {
        return when {
            this <= 0 && hungry -> COLUMNS
            else -> (this / columnWidth)
                .ceilToInt()
                .toUInt()
                .coerceAtMost(COLUMNS) // due to the inaccuracy of floating-point calculations
        }
    }

    private inner class ItemObserver : RecyclerView.AdapterDataObserver() {

        override fun onChanged() = Unit

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = Unit

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            cells.clear(positionStart, positionStart + itemCount)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            for (i in positionStart..<(positionStart + itemCount)) {
                val cached = cache[adapter.getItemId(i)]
                if (cached != null) {
                    cells.add(i, cached)
                } else {
                    cells.clear(i)
                    break
                }
            }
            repeatTrigger()
        }
    }

    private inner class RepeatTrigger : View.OnLayoutChangeListener {

        val parent get() = recyclerView.parent as View

        override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
            parent.removeOnLayoutChangeListener(this)
            recyclerView.requestLayout()
        }

        operator fun invoke() {
            parent.addOnLayoutChangeListener(this)
        }
    }
}