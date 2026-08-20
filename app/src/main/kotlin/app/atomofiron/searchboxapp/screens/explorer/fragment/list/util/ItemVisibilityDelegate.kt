package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter

class ItemVisibilityDelegate<D : Any>(
    private val adapter: GeneralAdapter<D, *>,
    private val listener: ItemVisibilityListener<D>,
) {

    val visibleItems: Set<Int>
        field = mutableSetOf()

    fun onItemAttached(holder: RecyclerView.ViewHolder) {
        if (holder.bindingAdapterPosition < 0) return
        visibleItems.add(holder.bindingAdapterPosition)
        val item = adapter.items[holder.bindingAdapterPosition]
        listener.onItemsBecomeVisible(listOf(item))
    }

    fun onItemDetached(holder: RecyclerView.ViewHolder) {
        if (holder.bindingAdapterPosition < 0) return
        visibleItems.remove(holder.bindingAdapterPosition)
    }

    fun interface ItemVisibilityListener<D> {
        fun onItemsBecomeVisible(items: List<D>)
    }
}