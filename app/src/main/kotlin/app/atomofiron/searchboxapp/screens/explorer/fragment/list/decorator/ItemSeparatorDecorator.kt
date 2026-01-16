package app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.drawable.colorSurfaceContainer

class ItemSeparatorDecorator : RecyclerView.ItemDecoration() {

    private val paint = Paint()

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        paint.color = parent.context.colorSurfaceContainer()
        paint.strokeWidth = parent.resources.getDimension(R.dimen.stroke_width)
    }

    override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(canvas, parent, state)

        parent.children.forEach { child ->
            val startX = parent.paddingLeft.toFloat()
            val stopX = parent.width - parent.paddingRight.toFloat()
            val y = child.run { y + height }
            canvas.drawLine(startX, y, stopX, y, paint)
        }
    }
}