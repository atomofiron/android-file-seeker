package app.atomofiron.searchboxapp.screens.explorer.fragment.list

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator.RootItemPaddingDecorator
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter

const val EXPLORER_SPAN_COUNT = 144

class ExplorerSpanSizeLookup(
    private val recyclerView: RecyclerView,
    private val rootsAdapter: RootAdapter,
    private val marginDecorator: RootItemPaddingDecorator,
) : GridLayoutManager.SpanSizeLookup() {

    private val minWidth = recyclerView.resources.getDimensionPixelSize(R.dimen.column_min_width)
    private val rootCount: Int get() = rootsAdapter.itemCount
    private var spanArr = IntArray(0)

    override fun getSpanSize(position: Int): Int = when {
        position < rootCount -> spanArr[position]
        else -> EXPLORER_SPAN_COUNT
    }

    fun update(availableWidth: Int) {
        if (rootCount == 0) {
            return
        }
        val frameWidth = availableWidth - recyclerView.run { paddingStart + paddingEnd }
        val spanWidth = frameWidth / EXPLORER_SPAN_COUNT
        val column = minWidth / spanWidth //+?
        val columns = EXPLORER_SPAN_COUNT / column
        val rowCount = rootCount / columns + if ((rootCount % columns) == 0) 0 else 1
        var count = rootCount
        val rows = IntArray(rowCount) {
            count -= columns
            if (count < 0) count + columns
            else columns
        }
        var free = columns - rows.last() - 1
        while (free > 0) {
            for (i in rows.lastIndex.dec() downTo 0) {
                free -= 1
                rows[rows.lastIndex] += 1
                rows[i] -= 1
                if (free == 0) break
            }
            free -= 1
        }
        spanArr = IntArray(rootCount)
        var index = 0
        for (i in rows.indices) {
            val row = rows[i]
            for (j in 0..<row) {
                spanArr[index++] = EXPLORER_SPAN_COUNT / row
            }
        }
        marginDecorator.update(rows)
    }
}