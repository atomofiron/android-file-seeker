package app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.isRtl
import lib.atomofiron.insets.ExtendedWindowInsets
import lib.atomofiron.insets.InsetsListener
import kotlin.math.max

class RootItemMarginDecorator(resources: Resources) : RecyclerView.ItemDecoration(), InsetsListener {

    private val outerMargin = resources.getDimensionPixelSize(R.dimen.padding_common)
    private val innerMargin = resources.getDimensionPixelSize(R.dimen.padding_half)
    private var margins = IntArray(2)
    private val cellCount get() = margins.size / 2
    private var leftMargin
        get() = margins.first()
        set(value) = margins.set(0, value)
    private var rightMargin
        get() = margins.last()
        set(value) = margins.set(margins.lastIndex, value)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (view.id != R.id.item_explorer_card) {
            return
        }
        val spanCount = (parent.layoutManager as GridLayoutManager).spanCount
        update(cellCount = spanCount)
        var position = parent.getChildLayoutPosition(view)
        position %= spanCount
        if (parent.isRtl()) {
            position = spanCount.dec() - position
        }
        val index = position * 2
        outRect.left = margins[index]
        outRect.right = margins[index.inc()]
    }

    override fun onApplyWindowInsets(windowInsets: ExtendedWindowInsets) {
        val cutout = windowInsets[ExtType { displayCutout + dock }]
        update(leftMargin = max(outerMargin - cutout.left, 0), rightMargin = max(outerMargin - cutout.right, 0))
    }

    private fun update(
        cellCount: Int = this.cellCount,
        leftMargin: Int = this.leftMargin,
        rightMargin: Int = this.rightMargin,
    ) {
        when {
            cellCount != this.cellCount -> Unit
            leftMargin != this.leftMargin -> Unit
            rightMargin != this.rightMargin -> Unit
            else -> return
        }
        margins = IntArray(cellCount * 2)
        this.leftMargin = leftMargin
        this.rightMargin = rightMargin
        updateMargins()
    }

    private fun updateMargins() {
        val spanCount = margins.size / 2
        var internal = innerMargin * spanCount.dec()
        val sum = leftMargin + rightMargin + internal
        val avg = sum / spanCount
        // распределяем отступы так, чтобы ячейки стали одинаковой ширины
        for (i in 0..<spanCount) {
            if (i == 0) {
                val other = avg - margins[0]
                margins[1] = other
                internal -= avg
            } else {
                val index = i * 2
                val left = innerMargin - margins[index.dec()]
                margins[index] = left
                margins[index.inc()] = avg - left
                internal -= avg
            }
        }
    }
}