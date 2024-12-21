package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.model.explorer.Node

class OnScrollIdleSubmitter(
    recyclerView: RecyclerView,
    private val adapter: ListAdapter<Node, *>,
    private val visibleItems: Set<Int>,
) : RecyclerView.OnScrollListener() {

    private var items: List<Node>? = null
    private var marker: String? = null
    private var allowed = true

    init {
        recyclerView.addOnScrollListener(this)
    }

    fun submitListOnIdle(items: List<Node>, marker: String? = null) {
        if (allowed || marker != this.marker) {
            this.marker = marker
            adapter.submitList(items)
            items.triggerLastFiles()
        } else {
            this.items = items
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        allowed = newState == RecyclerView.SCROLL_STATE_IDLE
        if (allowed) {
            items?.let {
                items = null
                adapter.submitList(it)
            }
        }
    }

    // to update decoration offsets
    private fun List<Node>.triggerLastFiles() {
        val lastIndex = lastIndex
        visibleItems.forEach { i ->
            when {
                i >= lastIndex -> Unit
                get(i).parentPath.length <= get(i.inc()).parentPath.length -> Unit
                else -> adapter.notifyItemChanged(i)
            }
        }
    }
}