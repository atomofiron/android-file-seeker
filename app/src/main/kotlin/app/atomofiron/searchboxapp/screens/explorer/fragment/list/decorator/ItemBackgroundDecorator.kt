package app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.drawable.colorSurfaceContainer
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.TAG_EXPLORER_OPENED_ITEM
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.getSortedChildren

class ItemBackgroundDecorator(private val evenNumbered: Boolean) : RecyclerView.ItemDecoration() {

    private var cornerRadius = 0f
    private val paint = Paint()
    var enabled = false

    fun init(resources: Resources) {
        cornerRadius = resources.getDimension(R.dimen.explorer_border_width)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        paint.color = parent.context.colorSurfaceContainer()
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
                var left = parent.paddingLeft.toFloat()
                var right = parent.paddingRight.toFloat()
                if (left == 0f) left = -cornerRadius
                if (right == 0f) right = -cornerRadius
                canvas.drawRoundRect(left, child.top.toFloat(), parent.width - right, child.bottom.toFloat(), cornerRadius, cornerRadius, paint)
            }
        }
    }
}