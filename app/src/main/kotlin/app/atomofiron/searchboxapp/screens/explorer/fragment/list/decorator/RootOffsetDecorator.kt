package app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator

import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.LayoutDelegate.addPostLayoutListener
import app.atomofiron.searchboxapp.model.Layout
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerSpanSizeLookup
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter

class RootOffsetDecorator(
    recyclerView: RecyclerView,
    private val rootAdapter: RootAdapter,
    private val layoutManager: GridLayoutManager,
    private val spanSizeLookup: ExplorerSpanSizeLookup,
) : RecyclerView.ItemDecoration() {

    private val padding = recyclerView.resources.getDimensionPixelSize(R.dimen.padding_half)
    private var layout = Layout.Stub
    private val rootView = LayoutInflater.from(recyclerView.context)
        .inflate(R.layout.item_explorer_card, recyclerView, false)

    init {
        val wrapContent = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        rootView.measure(wrapContent, wrapContent)
        recyclerView.addPostLayoutListener { layout = it }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (view.id != R.id.item_explorer_card) {
            return
        }
        val holder = parent.getChildViewHolder(view)
        val cells = cells()
        if (holder.bindingAdapterPosition < cells && layout.isBottom) {
            outRect.top = offset(parent, cells)
        }
    }

    fun cells(): Int = spanSizeLookup.getSpanSizeOrNull(0)
        ?.let { layoutManager.spanCount / it }
        ?: 0

    fun offset(recyclerView: RecyclerView) = offset(recyclerView, cells())

    private fun offset(recyclerView: RecyclerView, cells: Int): Int {
        val roots = rootAdapter.itemCount
        val rows = roots / cells + if ((roots % cells) == 0) 0 else 1
        val rootHeight = rootView.measuredHeight * rows + padding * rows.inc()
        val space = recyclerView.run { height - paddingTop - paddingBottom }
        return when {
            rootHeight >= space -> 0
            else -> space - rootHeight
        }
    }
}
