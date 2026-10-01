package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewParent
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.utils.isLayoutRtl
import com.google.android.material.appbar.AppBarLayout

class WideRecyclerView : RecyclerView {

    private var horizontalPadding = 0
    private var parentView: View? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setHorizontalPadding(@DimenRes dimenId: Int = 0) {
        horizontalPadding = when (dimenId) {
            0 -> 0
            else -> resources.getDimensionPixelSize(dimenId)
        }
        tryUpdate(parentView ?: return)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        parentView = findParent()
        tryUpdate(parentView ?: return)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        parentView = null
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val parent = parentView ?: return
        val customWidthSpec = MeasureSpec.makeMeasureSpec(parent.measuredWidth, MeasureSpec.getMode(widthSpec))
        super.onMeasure(customWidthSpec, heightSpec)
        tryUpdate(parent)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val parent = parentView ?: return
        tryUpdate(parent)
        when {
            left == 0 && right == parent.measuredWidth -> super.onLayout(changed, left, top, right, bottom)
            else -> super.layout(0, top, parent.measuredWidth, bottom)
        }
    }

    private fun findParent(): View? = parent?.findParent()

    private fun ViewParent.findParent(): View? = when (this) {
        is AppBarLayout,
        is RecyclerView -> this
        else -> parent?.findParent()
    }

    private fun tryUpdate(parent: View) {
        val dx = when {
            this.parent === parent -> 0f
            else -> -parent.paddingStart.toFloat() // this.parent is the child of parentView
        }
        val start = parent.paddingStart + horizontalPadding
        val end = parent.paddingEnd + horizontalPadding
        if (dx != translationX || start != paddingStart || end != paddingEnd) {
            updatePadding(start, end)
            translationX = dx
        }
    }

    private fun updatePadding(paddingStart: Int, paddingEnd: Int) {
        if (paddingStart == this.paddingStart && paddingEnd == this.paddingEnd) {
            return
        }
        val child = findChildOnPaddingEdge()
        val position = child?.let { getChildLayoutPosition(it) }
        position?.let { postChildrenOffsetFix(position, child) }
        setPaddingRelative(paddingStart, paddingTop, paddingEnd, paddingBottom)
        position?.let { scrollToPosition(it) }
    }

    private fun findChildOnPaddingEdge(): View? {
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

    private fun postChildrenOffsetFix(position: Int, child: View) {
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