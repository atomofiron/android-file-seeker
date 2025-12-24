package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter

class ItemVisibilityDelegate<D : Any>(
    private val adapter: GeneralAdapter<D, *>,
    private val listener: ItemVisibilityListener<D>,
) {

    private val _visibleItems = mutableSetOf<Int>()
    val visibleItems: Set<Int> = _visibleItems

    fun onItemAttached(holder: RecyclerView.ViewHolder) {
        if (holder.bindingAdapterPosition < 0) return
        _visibleItems.add(holder.bindingAdapterPosition)
        val item = adapter.items[holder.bindingAdapterPosition]
        listener.onItemsBecomeVisible(listOf(item))
    }

    fun onItemDetached(holder: RecyclerView.ViewHolder) {
        if (holder.bindingAdapterPosition < 0) return
        _visibleItems.remove(holder.bindingAdapterPosition)
    }

    fun interface ItemVisibilityListener<D> {
        fun onItemsBecomeVisible(items: List<D>)
    }
}