package app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemGravityDecorator : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildLayoutPosition(view)
        when {
            position != 0 -> return
            parent.height <= parent.width -> return
        }
        val offset = parent.height - parent.paddingTop - parent.paddingBottom
        outRect.top += offset / 3 * 2
    }
}