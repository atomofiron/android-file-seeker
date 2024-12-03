package app.atomofiron.searchboxapp.screens.explorer.fragment

import android.content.res.Resources
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import kotlin.math.abs
import kotlin.math.sign

class SwipeMarkerDelegate(resources: Resources) : RecyclerView.OnItemTouchListener {

    private val allowedAria = resources.getDimensionPixelSize(R.dimen.edge_size)
    private var prevIndex = -1
    private var downChild: View? = null
    private var allowed = false
    private var makeChecked: Boolean? = null

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        if (disallowIntercept) allowed = false
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN) {
            val child = rv.findChildViewUnder(e.x, e.y)
            if (child?.id != R.id.item_explorer) {
                allowed = false
                return false
            }
            downChild = child
            prevIndex = downChild?.let { rv.getChildViewHolder(it).layoutPosition } ?: -1
            val end = rv.width - rv.paddingEnd
            allowed = e.x.toInt() in (end - allowedAria)..end
            makeChecked = null
        }
        return allowed
    }

    override fun onTouchEvent(recyclerView: RecyclerView, e: MotionEvent) {
        downChild?.let { child ->
            downChild = null
            val checkbox = child.getCheckBox()
            checkbox ?: return@let
            makeChecked = !checkbox.isChecked
            checkbox.isChecked = !checkbox.isChecked
        }
        val child = recyclerView.findChildViewUnder(e.x, e.y)
        child ?: return
        child.tryCheck()
        recyclerView.checkMissed(child)
    }

    private fun View.tryCheck() {
        val checkbox = getCheckBox()
        checkbox ?: return
        if (makeChecked == null) {
            makeChecked = !checkbox.isChecked
        }
        if (makeChecked != checkbox.isChecked) {
            checkbox.isChecked = !checkbox.isChecked
        }
    }

    private fun RecyclerView.checkMissed(child: View) {
        val prevIndex = prevIndex
        if (prevIndex < 0) return
        var index = getChildViewHolder(child).layoutPosition
        this@SwipeMarkerDelegate.prevIndex = index
        index = getChildViewHolder(child).layoutPosition.stepTo(prevIndex)
        if (prevIndex == index) return
        for (i in 0 until abs(prevIndex - index)) {
            val position = index
            index = index.stepTo(prevIndex)
            val missed = findViewHolderForLayoutPosition(position)?.itemView
            missed?.tryCheck()
        }
    }

    private fun Int.stepTo(target: Int): Int = this + (target - this).sign

    private fun View.getCheckBox(): CheckBox? = findViewById(R.id.item_explorer_cb)
}
