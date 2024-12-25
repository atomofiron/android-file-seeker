package app.atomofiron.searchboxapp.screens.explorer.fragment

import android.content.res.Resources
import android.graphics.PointF
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.utils.isRtl
import kotlin.math.abs
import app.atomofiron.common.util.progressionTo

private data class State(
    val toChecked: Boolean,
    val prevIndex: Int,
)

class SwipeMarkerDelegate(resources: Resources) {

    private val isRtl = resources.isRtl()
    private val allowedAria = resources.getDimensionPixelSize(R.dimen.edge_size)
    private var state: State? = null
    private var downPoint: PointF? = null

    fun onDown(rv: RecyclerView, x: Float, y: Float): Boolean {
        val itemView = rv.findChildViewUnder(x, y)
        if (itemView?.id != R.id.item_explorer) {
            return false
        }
        val area = when {
            isRtl -> rv.paddingLeft.let { it..(it + allowedAria) }
            else -> (rv.width - rv.paddingRight).let { (it - allowedAria)..it }
        }
        if (x.toInt() in area) {
            val prevIndex = rv.getChildLayoutPosition(itemView)
            val check = itemView.getCheckBox() ?: return false
            state = State(!check.isChecked, prevIndex)
            downPoint = PointF(x, y)
        }
        return state != null
    }

    fun onMove(rv: RecyclerView, x: Float, y: Float): Boolean {
        downPoint?.let {
            if (abs(it.x - x) > abs(it.y - y)) {
                state = null
            }
        }
        downPoint = null
        return rv.check(x, y)
    }

    fun onUp(rv: RecyclerView, x: Float, y: Float) {
        rv.check(x, y)
        state = null
    }

    private fun RecyclerView.check(x: Float, y: Float): Boolean {
        val state = state ?: return false
        val itemView = findChildViewUnder(x, y)
        if (itemView?.id != R.id.item_explorer) {
            return true
        }
        val index = getChildLayoutPosition(itemView)
        var toggled = false
        for (i in index.progressionTo(state.prevIndex)) {
            val checkBox = findViewHolderForLayoutPosition(i)
                ?.itemView
                ?.getCheckBox()
                ?: continue
            toggled = toggled || checkBox.isChecked != state.toChecked
            checkBox.isChecked = state.toChecked
        }
        this@SwipeMarkerDelegate.state = state.copy(prevIndex = index)
        if (toggled) performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
        return true
    }

    private fun View.getCheckBox(): CheckBox? = findViewById(R.id.item_explorer_cb)
}
