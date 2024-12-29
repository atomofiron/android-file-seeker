package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerAdapter

class ItemVisibilityDelegate(
    private val adapter: ExplorerAdapter,
    private val listener: ExplorerItemVisibilityListener,
) {

    private val _visibleItems = mutableSetOf<Int>()
    val visibleItems: Set<Int> = _visibleItems

    fun onItemAttached(holder: RecyclerView.ViewHolder) {
        if (holder.bindingAdapterPosition < 0) return
        _visibleItems.add(holder.bindingAdapterPosition)
        listener.onItemBecomeVisible(adapter.currentList[holder.bindingAdapterPosition])
    }

    fun onItemDetached(holder: RecyclerView.ViewHolder) {
        if (holder.bindingAdapterPosition < 0) return
        _visibleItems.remove(holder.bindingAdapterPosition)
    }

    interface ExplorerItemVisibilityListener {
        fun onItemBecomeVisible(item: Node)
    }
}