package app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.TAG_EXPLORER_OPENED_ITEM
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.getSortedChildren

class ItemBackgroundDecorator(private val evenNumbered: Boolean) : RecyclerView.ItemDecoration() {

    private val paint = Paint()
    var enabled = false

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        paint.color = ContextCompat.getColor(parent.context, R.color.surface_variant_lite)
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)

        if (!enabled) return

        parent.getSortedChildren().forEach {
            val child = it.value
            if (child.id != R.id.item_explorer) return@forEach
            if (child.tag == TAG_EXPLORER_OPENED_ITEM) return@forEach

            val holder = parent.getChildViewHolder(child)
            val position = holder.bindingAdapterPosition

            if ((position % 2 == 0) == evenNumbered) {
                canvas.drawRect(0f, child.top.toFloat(), parent.width.toFloat(), child.bottom.toFloat(), paint)
            }
        }
    }
}