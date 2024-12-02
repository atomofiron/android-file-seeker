package app.atomofiron.searchboxapp.screens.explorer.fragment

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
import kotlin.math.min

class ExplorerSpanSizeLookup(
    recyclerView: RecyclerView,
    private val layoutManager: GridLayoutManager,
    private val rootsAdapter: RootAdapter,
) : GridLayoutManager.SpanSizeLookup() {

    private val itemCount: Int get() = rootsAdapter.itemCount
    private val spanCount: Int get() = layoutManager.spanCount

    init {
        recyclerView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateSpanCount(recyclerView)
        }
    }

    override fun getSpanSize(position: Int): Int {
        val remnant = itemCount % spanCount
        return when {
            position >= itemCount -> spanCount
            position < (itemCount - remnant) -> 1
            remnant > (spanCount / 2) -> 1
            else -> 2
        }
    }

    fun updateSpanCount(recyclerView: RecyclerView) {
        val spanCount = recyclerView.run {
            val margin = resources.getDimensionPixelSize(R.dimen.content_margin)
            val minWidth = margin + resources.getDimensionPixelSize(R.dimen.column_min_width)
            var frameWidth = width
            if (frameWidth == 0) {
                frameWidth = resources.displayMetrics.widthPixels
            }
            frameWidth -= paddingStart + paddingEnd + margin

            var calculated = frameWidth / minWidth
            if (calculated < 3) {
                val compactWidth = margin + resources.getDimensionPixelSize(R.dimen.compact_column_min_width)
                calculated = frameWidth / compactWidth
            }
            when (val itemCount = itemCount) {
                0 -> calculated
                else -> min(itemCount, calculated)
            }
        }
        if (spanCount > 0 && spanCount != this.spanCount) {
            layoutManager.spanCount = spanCount
            val remnant = itemCount % spanCount
            rootsAdapter.verticalCount = itemCount - if (remnant > spanCount / 2) 0 else remnant
        }
    }
}