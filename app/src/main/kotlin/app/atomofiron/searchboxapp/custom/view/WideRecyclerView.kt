package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.View
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.utils.isLayoutRtl

class WideRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val parent = parent as View
        tryUpdate(parent)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        val parent = parent as View
        tryUpdate(parent)
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val parent = parent as View
        tryUpdate(parent)
        val customWidthSpec = MeasureSpec.makeMeasureSpec(parent.width, MeasureSpec.getMode(widthSpec))
        super.onMeasure(customWidthSpec, heightSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, top: Int, r: Int, bottom: Int) {
        val parent = parent as View
        super.onLayout(changed, 0, top, parent.width, bottom)
    }

    private fun tryUpdate(parent: View) {
        if (paddingStart != parent.paddingStart || paddingEnd != parent.paddingEnd) {
            updatePadding(parent.paddingStart, parent.paddingEnd)
            updateLayoutParams<MarginLayoutParams> {
                marginStart = -parent.paddingStart
                marginEnd = -parent.paddingEnd
            }
        }
    }

    private fun View.updatePadding(paddingStart: Int, paddingEnd: Int) {
        val child = takeIf { paddingStart != this.paddingStart }?.let {
            findChildOnPaddingEdge()
        }
        val position = child?.let { getChildLayoutPosition(it) }
        position?.let { postChildrenOffsetFix(position, child) }
        setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom)
        position?.let { scrollToPosition(it) }
    }

    private fun RecyclerView.findChildOnPaddingEdge(): View? {
        var offset = 0
        while (offset < width) {
            val paddingEdge = when {
                isLayoutRtl -> width - paddingStart - offset
                else -> paddingStart + offset
            }
            val y = (paddingTop + height - paddingBottom) / 2f
            findChildViewUnder(paddingEdge.toFloat(), y)?.let {
                return it
            }
            offset += 8
        }
        return null
    }

    private fun RecyclerView.postChildrenOffsetFix(position: Int, child: View) {
        val childOffset = child.start - paddingStart
        post {
            val holder = findViewHolderForLayoutPosition(position)
            holder ?: return@post
            val newChildOffset = holder.itemView.start - paddingStart
            var dif = newChildOffset - childOffset
            if (isLayoutRtl) dif *= -1
            scrollBy(dif, 0)
        }
    }

    private val View.start: Int get() = if (isLayoutRtl) ((parent as View).width - right) else left
}