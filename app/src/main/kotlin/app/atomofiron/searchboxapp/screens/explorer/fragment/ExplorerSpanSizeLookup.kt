package app.atomofiron.searchboxapp.screens.explorer.fragment

import android.content.res.Resources
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
import kotlin.math.max

class ExplorerSpanSizeLookup(
    private val resources: Resources,
    private val layoutManager: GridLayoutManager,
    private val rootsAdapter: RootAdapter,
) : GridLayoutManager.SpanSizeLookup() {

    private val minWidth = resources.getDimensionPixelSize(R.dimen.column_min_width)
    private val maxWidth = resources.getDimensionPixelSize(R.dimen.column_max_width)

    private val rootCount: Int get() = rootsAdapter.itemCount
    private val spanCount: Int get() = layoutManager.spanCount

    override fun getSpanSize(position: Int): Int = if (position < rootCount) 1 else spanCount

    fun updateSpanCount(recyclerView: RecyclerView) {
        val frameWidth = let {
            val width = recyclerView.width
            val layoutWidth = if (width == 0) resources.displayMetrics.widthPixels else width
            layoutWidth - recyclerView.run { paddingStart + paddingEnd }
        }
        var spanCount = frameWidth / minWidth
        if (spanCount > rootCount) {
            val minSpanCount = frameWidth / maxWidth
            spanCount = max(minSpanCount, rootCount)
        }
        if (spanCount > 0 && spanCount != this.spanCount) {
            layoutManager.spanCount = spanCount
            rootsAdapter.notifyItemRangeChanged(0, rootCount)
        }
    }
}