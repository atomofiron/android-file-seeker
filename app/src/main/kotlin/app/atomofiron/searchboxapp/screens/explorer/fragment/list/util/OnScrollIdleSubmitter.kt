package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class OnScrollIdleSubmitter<T>(
    recyclerView: RecyclerView,
    private val adapter: ListAdapter<T, *>,
) : RecyclerView.OnScrollListener() {

    private var items: List<T>? = null
    private var marker: String? = null
    private var allowed = true

    init {
        recyclerView.addOnScrollListener(this)
    }

    fun trySubmitList(items: List<T>, marker: String? = null) {
        if (allowed || marker != this.marker) {
            this.marker = marker
            adapter.submitList(items)
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
}