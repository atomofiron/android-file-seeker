package app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.extension.debugRequire
import app.atomofiron.common.util.extension.resizeWith
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.isRtl
import lib.atomofiron.insets.ExtendedWindowInsets
import lib.atomofiron.insets.InsetsListener
import kotlin.math.max

class RootItemPaddingDecorator(
    resources: Resources,
    private val adapter: RootAdapter,
) : RecyclerView.ItemDecoration(), InsetsListener {

    private val outerPadding = resources.getDimensionPixelSize(R.dimen.padding_common)
    private val innerPadding = resources.getDimensionPixelSize(R.dimen.padding_half)
    private var rows = IntArray(0)
    private var paddings = mutableListOf<Int>()
    private var leftPadding = 0
    private var rightPadding = 0

    fun update(rows: IntArray) {
        if (!rows.contentEquals(this.rows)) {
            this.rows = rows
            adapter.notifyDataSetChanged()
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (view.id != R.id.item_explorer_card) {
            return
        }
        var sum = 0
        var index = 0
        val position = parent.getChildLayoutPosition(view)
        val cellCount = rows.find {
            index = position - sum
            sum += it
            sum >= position.inc()
        } ?: return debugRequire(false) { "position $position rows ${rows.toList()}" }
        if (parent.isRtl()) {
            index = cellCount.dec() - index
        }
        index *= 2
        val paddings = getPaddings(cellCount)
        outRect.left = paddings[index]
        outRect.right = paddings[index.inc()]
    }

    override fun onApplyWindowInsets(windowInsets: ExtendedWindowInsets) {
        val cutout = windowInsets[ExtType { displayCutout + dock }]
        val left = max(0, outerPadding - cutout.left)
        val right = max(0, outerPadding - cutout.right)
        if (left != leftPadding || right != rightPadding) {
            leftPadding = left
            rightPadding = right
            paddings.resizeWith(2, 0)
            paddings[0] = leftPadding
            paddings[1] = rightPadding
        }
    }

    private fun getPaddings(cellCount: Int): List<Int> {
        if (cellCount == paddings.size / 2) {
            return paddings
        }
        paddings.resizeWith(cellCount * 2, 0)
        paddings[0] = leftPadding
        val spanCount = paddings.size / 2
        val sum = leftPadding + rightPadding + innerPadding * spanCount.dec()
        val avg = sum / spanCount
        // distribute the paddings so that the cells become the same width
        for (i in 0..<spanCount) {
            if (i == 0) {
                paddings[1] = avg - paddings[0]
            } else {
                val index = i * 2
                val left = innerPadding - paddings[index.dec()]
                paddings[index] = left
                paddings[index.inc()] = avg - left
            }
        }
        return paddings
    }
}