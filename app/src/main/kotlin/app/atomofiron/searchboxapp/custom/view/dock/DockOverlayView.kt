package app.atomofiron.searchboxapp.custom.view.dock

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.core.view.children
import app.atomofiron.common.util.MaterialDimen
import app.atomofiron.common.util.noClip
import app.atomofiron.fileseeker.R

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class DockOverlayView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    private val dockBarView: DockViewImpl,
) : FrameLayout(context, attrs, defStyleAttr), DockView by dockBarView, Forwarding by dockBarView {

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : this(context, attrs, defStyleAttr, DockViewImpl(context))

    init {
        noClip()
        translationZ = resources.getDimension(MaterialDimen.m3_comp_navigation_bar_container_elevation)
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        if (params != null && dockBarView.parent == null) {
            dockBarView.layoutParams = LayoutParams(params.width, params.height)
            addView(dockBarView)
        }
        params?.width = MATCH_PARENT
        params?.height = MATCH_PARENT
        super.setLayoutParams(params)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount == 1) for (child in dockBarView.children) {
            val popup = child.findViewById<ViewGroup>(R.id.popup).getChildAt(0)
            popup ?: continue
            val moved = event.offset(-(dockBarView.x + child.x), -(dockBarView.y + child.y))
            if (popup.dispatchTouchEvent(moved)) {
                return true
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun MotionEvent.offset(dx: Float, dy: Float) = MotionEvent.obtain(downTime, eventTime, action, x + dx, y + dy, pressure, size, metaState, xPrecision, yPrecision, deviceId, edgeFlags)
}

interface Forwarding {
    fun setPadding(left: Int, top: Int, right: Int, bottom: Int)
    fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int)
    fun getPaddingStart(): Int
    fun getPaddingTop(): Int
    fun getPaddingEnd(): Int
    fun getPaddingBottom(): Int
    fun getPaddingLeft(): Int
    fun getPaddingRight(): Int
}
