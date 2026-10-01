package app.atomofiron.common.recycler

import android.content.res.Resources
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.extension.ceilToInt
import app.atomofiron.common.util.extension.clear
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.layout.MeasuringRecyclerView

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
    val span: Int,
)

interface AdapterHolderListener {
    fun onCreate(holder: Holder, viewType: Int)
    fun onBind(holder: Holder, position: Int)
}

class FlexSpanSizeLookup(
    private val recyclerView: MeasuringRecyclerView,
    private val adapter: ListAdapter<out GeneralItem, *>,
    private val manager: GridLayoutManager,
    resources: Resources,
)  : GridLayoutManager.SpanSizeLookup(), AdapterHolderListener {

    private val portraitWidth = resources.getDimension(R.dimen.screen_compact)
    private val itemCount get() = adapter.itemCount
    private val holders = mutableListOf<Holder>()
    private val cache = hashMapOf<Long,Cell>()
    private val cells = mutableListOf<Cell>()

    private var availableArea = 0f
    private var columnWidth = 0f
    private var portraitColumns = 0u
    private val repeatTrigger = this@FlexSpanSizeLookup.RepeatTrigger()

    init {
        manager.spanCount = COLUMNS_INT
        manager.spanSizeLookup = this
        adapter.registerAdapterDataObserver(this@FlexSpanSizeLookup.ItemObserver())
        recyclerView.addMeasureListener { width, _ -> updateArea(width) }
    }

    override fun onCreate(holder: Holder, viewType: Int) {
        if (holders.none { it === holder }) {
            holders.add(holder)
        }
    }

    override fun onBind(holder: Holder, position: Int) {
        val minWidth = holder.minWidth().ceilToInt()
        val cell = cells.getOrNull(position)
        when (true) {
            (cell == null),
            (cell.width != minWidth),
            (cell.hungry != holder.hungry),
            (cell.type != holder.itemViewType) -> calcSize(position, minWidth)
            else -> Unit
        }
    }

    private fun updateArea(width: Int = recyclerView.availableWidth) {
        val available = width.toFloat() - recyclerView.run { paddingStart + paddingEnd }
        if (available > 0 && available != availableArea) {
            availableArea = available
            columnWidth = availableArea / COLUMNS_INT
            portraitColumns = (portraitWidth / columnWidth).toUInt().coerceAtLeast(1u)
            invalidateSpanGroupIndexCache()
            invalidateSpanIndexCache()
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
        val atPosition = cells.getOrNull(position)
        val cached = atPosition ?: cache[itemId]
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
            // not bound and never measured: occupies the whole row like a hungry item, onBind() will recalculate it
            minWidth = FILL_ROW.ceilToInt()
            type = adapter.getItemViewType(position)
            hungry = true
        }
        var rowId = when (position) {
            0 -> itemId.toInt()
            else -> cells.getOrNull(position.dec())?.rowId ?: itemId.toInt()
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
        val cell = Cell(minWidth, type, hungry, columns, rowId, UNDEFINED)
        when {
            position < cells.size -> cells[position] = cell
            else -> cells.add(position, cell)
        }
        val span = calcSpan(rowId, position, consumed + columns, columns)
        val filled = cell.copy(span = span)
        cells[position] = filled
        cache[itemId] = filled
        if (atPosition != null && atPosition.span != span) {
            repeatTrigger()
        }
        return span
    }

    private fun calcSpan(rowId: Int, position: Int, used: UInt, columns: UInt): Int {
        return when {
            !isComplete(rowId, position, used) -> columns
            availableArea <= portraitWidth && count(rowId) == 1 -> COLUMNS // consider like hungry
            else -> columns + calcFree(rowId, position)
        }.toInt()
    }

    private fun isComplete(rowId: Int, position: Int, used: UInt): Boolean {
        require(cells.size <= itemCount)
        require(cells.isNotEmpty())
        return when {
            used >= COLUMNS -> true // cannot accept anything more
            cells.size == itemCount -> true
            rowId != cells.last().rowId -> true
            !nextFits(position, used) -> true
            else -> false
        }
    }

    private fun nextFits(position: Int, used: UInt): Boolean {
        val next = position.inc()
        if (next >= itemCount) {
            return false // the last item closes its row
        }
        val columns = columnsAt(next) ?: return false // unknown item behaves like the hungry one, see calcSize
        return used + columns <= COLUMNS
    }

    private fun columnsAt(position: Int): UInt? {
        val holder = holderAt(position)
        if (holder != null) {
            return holder.minWidth().ceilToInt().toColumns(holder.hungry)
        }
        val cell = cells.getOrNull(position) ?: cache[adapter.getItemId(position)]
        return cell?.columns
    }

    private fun count(rowId: Int): Int = cells.count { it.rowId == rowId }

    private fun holderAt(position: Int): Holder? = holders.find { it.absoluteAdapterPosition == position }

    private fun consumed(rowId: Int, position: Int) = cells.asSequence()
        .filterIndexed { index, cell -> cell.rowId == rowId && index < position }
        .sumOf { it.columns }

    private fun calcFree(rowId: Int, position: Int): UInt {
        val row = cells.filter { it.rowId == rowId }
        val first = cells.indexOfFirst { it.rowId == rowId }
        val index = (position - first).coerceAtLeast(0)
        val cell = cells.getOrNull(position) ?: return 0u
        val prevRowCell = cells.getOrNull(first.dec())
        val hungry = when {
            cell.hungry -> row.count { it.hungry }.toUInt()
            row.size == 1 && prevRowCell?.let { it.type == cell.type } != true -> return 0u
            row.any { it.type != cell.type } -> return 0u
            // consider like hungry
            row.size == 1 -> return calcFree(prevRowCell!!.rowId, first.dec())
            else -> row.size.toUInt()
        }
        val free = COLUMNS - row.sumOf { it.columns }.coerceAtMost(COLUMNS)
        val supplement = free / hungry
        return when {
            index.toUInt() < (free % hungry) -> supplement.inc()
            else -> supplement
        }
    }

    private fun Int.toColumns(hungry: Boolean): UInt {
        return when {
            this <= FILL_ROW && hungry -> COLUMNS
            else -> (this / columnWidth)
                .ceilToInt()
                .toUInt()
                .coerceAtMost(COLUMNS) // due to the inaccuracy of floating-point calculations
        }
    }

    private inner class ItemObserver : RecyclerView.AdapterDataObserver() {

        override fun onChanged() = invalidate(0)

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            invalidate(minOf(fromPosition, toPosition))
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            invalidate(positionStart)
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            invalidate(positionStart)
        }

        private fun invalidate(position: Int) {
            cells.clear(position) // rowId of every following cell depends on the preceding ones
            repeatTrigger()
        }
    }

    private inner class RepeatTrigger : View.OnLayoutChangeListener {

        private var pending = false

        override fun onLayoutChange(view: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
            view.removeOnLayoutChangeListener(this)
            pending = false
            invalidateSpanIndexCache()
            recyclerView.requestLayout()
        }

        operator fun invoke() {
            if (pending) {
                return
            }
            val parent = recyclerView.parent as? View ?: return
            pending = true
            parent.addOnLayoutChangeListener(this)
        }
    }
}