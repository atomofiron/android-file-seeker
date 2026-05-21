package app.atomofiron.searchboxapp.screens.explorer.fragment

import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.CoroutineListDiffer
import app.atomofiron.searchboxapp.custom.LayoutDelegate.addPostLayoutListener
import app.atomofiron.searchboxapp.model.Layout
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerAdapter
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator.RootOffsetDecorator
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter

class ExplorerOffsetScroller(
    private val recyclerView: RecyclerView,
    private val decorator: RootOffsetDecorator,
) {
    companion object {
        fun RecyclerView.animateRootOffset(
            rootAdapter: RootAdapter,
            nodeAdapter: ExplorerAdapter,
            decorator: RootOffsetDecorator,
        ) {
            val animator = ExplorerOffsetScroller(this, decorator)
            nodeAdapter.addListListener(object : CoroutineListDiffer.ListListener<Node> {
                override fun onCurrentListChanged(current: List<Node>) = animator.onCurrentListChanged(current)
            })
            addPostLayoutListener {
                animator.layout = it
                rootAdapter.notifyItemRangeChanged(0, decorator.cells())
            }
        }
    }

    private var layout = Layout.Stub
    private var onlyRoots = true

    private fun onCurrentListChanged(current: List<Node>) {
        onlyRoots = current.isEmpty().also {
            if (it == onlyRoots) return
        }
        if (!onlyRoots && layout.isBottom) recyclerView.post {
            recyclerView.smoothScrollBy(0, decorator.offset(recyclerView))
        }
    }
}
