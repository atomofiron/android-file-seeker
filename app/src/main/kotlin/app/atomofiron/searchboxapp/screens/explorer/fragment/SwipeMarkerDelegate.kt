package app.atomofiron.searchboxapp.screens.explorer.fragment

import android.content.Context
import android.content.res.Resources
import android.graphics.PointF
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_SETTLING
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.utils.isRtl
import kotlin.math.abs
import app.atomofiron.common.util.progressionTo
import app.atomofiron.searchboxapp.utils.disallowInterceptTouches

private data class State(
    val toChecked: Boolean,
    val prevIndex: Int,
)

class SwipeMarkerDelegate(resources: Resources) {

    private val isRtl = resources.isRtl()
    private val allowedAria = resources.getDimensionPixelSize(R.dimen.edge_size)
    private var state: State? = null
    private var downPoint: PointF? = null

    private fun onDown(rv: RecyclerView, x: Float, y: Float): Boolean {
        if (rv.scrollState == SCROLL_STATE_SETTLING) {
            return false
        }
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

    private fun onMove(rv: RecyclerView, x: Float, y: Float): Boolean {
        downPoint?.let {
            if (abs(it.x - x) > abs(it.y - y)) {
                state = null
            }
        }
        downPoint = null
        return rv.check(x, y)
    }

    private fun onUp(rv: RecyclerView, x: Float, y: Float) {
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
        for (i in index progressionTo state.prevIndex) {
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

    fun onTouch(view: RecyclerView, event: MotionEvent): Boolean = when (event.action) {
        MotionEvent.ACTION_DOWN -> onDown(view, event.x, event.y)
            .also { if (it) view.parent.disallowInterceptTouches() }
        MotionEvent.ACTION_MOVE -> onMove(view, event.x, event.y)
        MotionEvent.ACTION_CANCEL,
        MotionEvent.ACTION_UP -> false.also { onUp(view, event.x, event.y) }
        else -> false
    }
}

class SwipeMarkerLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private val delegate = SwipeMarkerDelegate(resources)

    override fun addView(child: View?) {
        super.addView(child)
        if (child != null && child !is RecyclerView) {
            throw IllegalArgumentException()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val child = getChildAt(0) as RecyclerView
        return delegate.onTouch(child, event) || super.dispatchTouchEvent(event)
    }
}
