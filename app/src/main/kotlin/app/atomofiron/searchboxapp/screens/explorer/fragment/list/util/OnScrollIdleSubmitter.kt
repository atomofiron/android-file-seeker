package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.searchboxapp.model.explorer.Node

class OnScrollIdleSubmitter(
    recyclerView: RecyclerView,
    private val adapter: GeneralAdapter<Node,*>,
) : RecyclerView.OnScrollListener() {

    private var items: MutableList<Node>? = null
    private var marker: String? = null
    private var allowed = true

    init {
        recyclerView.addOnScrollListener(this)
        adapter.registerAdapterDataObserver(ChangeListener())
    }

    fun submitOnIdle(items: List<Node>, marker: String? = null) {
        if (allowed || marker != this.marker) {
            this.marker = marker
            adapter.submit(items)
        } else {
            this.items = items.toMutableList()
        }
    }

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

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        allowed = newState == RecyclerView.SCROLL_STATE_IDLE
        if (allowed) {
            adapter.submit(items ?: return)
            items = null
        }
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