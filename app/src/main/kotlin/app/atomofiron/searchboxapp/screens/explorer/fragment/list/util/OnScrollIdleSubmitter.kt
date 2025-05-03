package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.CoroutineListDiffer
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.searchboxapp.model.explorer.Node

class OnScrollIdleSubmitter(
    recyclerView: RecyclerView,
    private val adapter: GeneralAdapter<Node,*>,
    private val visibleItems: Set<Int>,
) : RecyclerView.OnScrollListener(), CoroutineListDiffer.ListListener<Node> {

    private var items: MutableList<Node>? = null
    private var marker: String? = null
    private var allowed = true

    init {
        recyclerView.addOnScrollListener(this)
        adapter.addListListener(this)
    }

    fun submitOnIdle(items: List<Node>, marker: String? = null) {
        if (allowed || marker != this.marker) {
            this.marker = marker
            adapter.submit(items)
            items.triggerLastFiles()
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
            (item.isCurrent != old.isCurrent) -> item.copy(isCurrent = old.isCurrent, children = item.children?.copy(isOpened = old.isOpened))
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
            items?.triggerLastFiles()
            items = null
        }
    }

    override fun onItemChanged(index: Int, item: Node) = Unit

    override fun onCurrentListChanged(current: List<Node>) = current.triggerLastFiles()

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