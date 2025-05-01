package app.atomofiron.searchboxapp.custom.view.dock

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.core.view.children
import app.atomofiron.common.util.noClip
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.popup.DockPopupLayout

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class DockBarView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    val dockView: DockViewImpl,
) : FrameLayout(context, attrs, defStyleAttr), DockView by dockView, Forwarding by dockView {

    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : this(context, attrs, defStyleAttr, DockViewImpl(context))

    init {
        noClip()
        translationZ = resources.getDimension(R.dimen.dock_elevation)
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        if (params != null && dockView.parent == null) {
            dockView.layoutParams = LayoutParams(params.width, params.height)
            addView(dockView)
        }
        params?.width = MATCH_PARENT
        params?.height = MATCH_PARENT
        super.setLayoutParams(params)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount == 1) for (child in dockView.children) {
            val container = child.findViewById<DockPopupLayout>(R.id.popup)
            val popup = container.getChildAt(0)
            popup ?: continue
            val dx = child.x + container.x + popup.x
            val dy = child.y + container.y + popup.y
            val moved = event.offset(-(dockView.x + dx), -(dockView.y + dy))
            if (popup.dispatchTouchEvent(moved)) {
                return true
            }
            when {
                event.action != ACTION_DOWN -> Unit
                moved.x.toInt() !in child.run { left..right } -> Unit
                moved.y.toInt() !in child.run { top..bottom } -> Unit
                else -> return true
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun MotionEvent.offset(dx: Float, dy: Float) = MotionEvent.obtain(downTime, eventTime, action, x + dx, y + dy, pressure, size, metaState, xPrecision, yPrecision, deviceId, edgeFlags)
}

@Suppress("unused")
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
