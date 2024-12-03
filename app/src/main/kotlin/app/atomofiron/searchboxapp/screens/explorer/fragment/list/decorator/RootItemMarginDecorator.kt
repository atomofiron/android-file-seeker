package app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import kotlin.math.roundToInt

class RootItemMarginDecorator : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        if (view.id != R.id.item_explorer_card) return

        var position = parent.getChildLayoutPosition(view)
        val layoutManager = parent.layoutManager as GridLayoutManager
        val spanCount = layoutManager.spanCount
        val spanSize = layoutManager.spanSizeLookup.getSpanSize(position)
        position += spanSize.dec()
        val margin = parent.resources.getDimension(R.dimen.content_margin)
        val count = spanCount / spanSize
        val cellIndex = position % count
        val sum = margin * count.inc()
        val avg = sum / count
        val piece = avg - margin

        // распредиляем отступы так,
        // чтобы ячейки стали одинаковой ширины
        val left = when (position % count) {
            0 -> margin
            else -> (margin - piece * cellIndex)
        }
        val right = when (position % count) {
            count.dec() -> margin
            else -> avg - left
        }
        outRect.left = left.roundToInt()
        outRect.right = right.roundToInt()
    }
}