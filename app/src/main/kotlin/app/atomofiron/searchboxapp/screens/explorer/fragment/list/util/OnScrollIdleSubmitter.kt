package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.searchboxapp.model.explorer.Node

class OnScrollIdleSubmitter(
    recyclerView: RecyclerView,
    private val adapter: GeneralAdapter<Node,*>,
) : RecyclerView.OnScrollListener() {

    private var items: MutableList<Node>? = null

    init {
        recyclerView.addOnScrollListener(this)
        adapter.registerAdapterDataObserver(ChangeListener())
    }

    fun submit(items: List<Node>) = adapter.submit(items)

    fun submit(item: Node) {
        val oldIndexDelayed = items?.indexOfFirst { it.uniqueId == item.uniqueId } ?: -1
        val oldIndex = adapter.items.indexOfFirst { it.uniqueId == item.uniqueId }
        val old = items?.getOrNull(oldIndexDelayed)
            ?: adapter.items.getOrNull(oldIndex)
            ?: return
        val new = when (true) {
            (item.isOpened != old.isOpened),
            (item.isDeepest != old.isDeepest) -> item.copy(isDeepest = old.isDeepest, children = item.children?.copy(isOpened = old.isOpened))
            else -> item
        }
        if (new.areContentsTheSame(old)) {
            return
        }
        if (oldIndexDelayed >= 0) items?.set(oldIndexDelayed, new)
        if (oldIndex >= 0) adapter.submit(new, oldIndex)
    }

    private inner class ChangeListener : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = onEvent(positionStart)
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = onEvent(positionStart)
        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            onEvent(fromPosition)
            onEvent(toPosition)
        }
        private fun onEvent(position: Int) {
            if (position > 0) {
                // to update decoration offsets
                adapter.notifyItemChanged(position.dec())
            }
        }
    }
}