package app.atomofiron.searchboxapp.screens.explorer.fragment

import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.LayoutDelegate.addPostLayoutListener
import app.atomofiron.searchboxapp.model.Layout
import app.atomofiron.searchboxapp.model.explorer.NodeId
import app.atomofiron.searchboxapp.model.explorer.NodeRoot
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator.RootOffsetDecorator
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter

class ExplorerOffsetScroller(
    private val recyclerView: RecyclerView,
) {
    companion object {
        fun RecyclerView.animateRootOffset(rootAdapter: RootAdapter) {
            val scroller = ExplorerOffsetScroller(this)
            rootAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = scroller.onRootsChanged(rootAdapter.currentList)
            })
            addPostLayoutListener {
                scroller.layout = it
                rootAdapter.notifyDataSetChanged()
            }
        }
    }

    private val padding = recyclerView.resources.getDimensionPixelSize(R.dimen.item_root_padding)
    private var layout = Layout.Stub
    private var selectedId: NodeId? = null

    private fun onRootsChanged(items: List<NodeRoot>) {
        val selected = items.find { it.isSelected }
        if (selected?.id != selectedId) {
            selectedId = selected?.id
            if (selectedId != null && layout.isBottom) {
                onSelected()
            }
        }
    }

    private fun onSelected() {
        recyclerView.post {
            val first = recyclerView.getChildAt(0)
                ?: return@post
            val offset = first.top - recyclerView.paddingTop - padding
            if (offset > 0) {
                recyclerView.smoothScrollBy(0, offset)
            }
        }
    }
}
