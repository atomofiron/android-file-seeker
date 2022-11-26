package app.atomofiron.searchboxapp.screens.finder.adapter

import androidx.recyclerview.widget.GridLayoutManager
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

class FinderSpanSizeLookup(
    private val adapter: FinderAdapter,
    private val manager: GridLayoutManager,
) : GridLayoutManager.SpanSizeLookup() {
    override fun getSpanSize(position: Int): Int {
        val item = adapter.currentList.getOrNull(position)
        item ?: return 1
        return when (item) {
            is FinderStateItem.ButtonsItem -> 1
            is FinderStateItem.TestItem -> 1
            else -> manager.spanCount
        }
    }
}