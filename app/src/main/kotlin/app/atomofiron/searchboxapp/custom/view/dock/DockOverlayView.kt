package app.atomofiron.searchboxapp.custom.view.dock

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.children
import app.atomofiron.common.util.MaterialDimen
import app.atomofiron.fileseeker.R
import kotlin.math.max

class DockOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var dock: DockBarView? = null

    init {
        translationZ = resources.getDimension(MaterialDimen.m3_comp_navigation_bar_container_elevation).inc()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val dock = dock
            ?.takeIf { event.pointerCount == 1 }
            ?: return false
        for (child in dock.children) {
            child.findViewById<ViewGroup>(R.id.popup)
                .getChildAt(0)
                ?.run {
                    val moved = event.offset(-(dock.x + child.x), -(dock.y + child.y))
                    dispatchTouchEvent(moved)
                }?.takeIf { it }
                ?.let { return true }
        }
        return false
    }

    fun setDockBarView(view: DockBarView) {
        dock = view
        translationZ = max(translationZ, view.elevation.inc())
    }

    private fun MotionEvent.offset(dx: Float, dy: Float) = MotionEvent.obtain(downTime, eventTime, action, x + dx, y + dy, pressure, size, metaState, xPrecision, yPrecision, deviceId, edgeFlags)
}