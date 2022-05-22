package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS
import com.google.android.material.shape.MaterialShapeUtils
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.getColorByAttr
import kotlin.math.max
import kotlin.math.min

class FixedBottomAppBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs), AttachedBehavior {
    private val materialShapeDrawable = MaterialShapeDrawable()
    private val mBehavior: Behavior
    private val scrollListener: ScrollListener

    init {
        materialShapeDrawable.shadowCompatibilityMode = SHADOW_COMPAT_MODE_ALWAYS
        materialShapeDrawable.paintStyle = Paint.Style.FILL
        materialShapeDrawable.initializeElevationOverlay(context)
        materialShapeDrawable.fillColor = ColorStateList.valueOf(context.getColorByAttr(android.R.attr.colorBackground))
        background = materialShapeDrawable

        val maxElevation = resources.getDimension(R.dimen.bottom_bar_elevation)
        scrollListener = ScrollListener(this, materialShapeDrawable, maxElevation)
        mBehavior = Behavior(context, scrollListener)
    }

    fun updateElevation() {
        scrollListener.updateElevation(null)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        MaterialShapeUtils.setParentAbsoluteElevation(this, materialShapeDrawable)
        (parent as ViewGroup).clipChildren = false
    }

    override fun getBehavior(): Behavior = mBehavior

    class Behavior(
        context: Context,
        private val onScrollListener: RecyclerView.OnScrollListener
    ) : FixedHideBottomViewOnScrollBehavior<FixedBottomAppBar>(context) {
        private var added = false

        override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FixedBottomAppBar, directTargetChild: View, target: View, nestedScrollAxes: Int, type: Int): Boolean {
            if (!added) {
                added = true
                target as RecyclerView
                target.addOnScrollListener(onScrollListener)
                target.addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
                    onScrollListener.onScrolled(view as RecyclerView, 0, 0)
                }
            }
            return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes, type)
        }
    }

    override fun setTranslationY(translationY: Float) {
        super.setTranslationY(translationY)
        scrollListener.updateElevation(null)
    }

    class ScrollListener(
        private val target: View,
        private val materialShapeDrawable: MaterialShapeDrawable,
        private val maxElevation: Float
    ) : RecyclerView.OnScrollListener() {
        private var childBottom: Int = 0
        private var reverseLayout: Boolean = false
        private var position: Int = -1

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (recyclerView.childCount == 0) {
                return
            }

            reverseLayout = (recyclerView.layoutManager as LinearLayoutManager).reverseLayout
            val child = recyclerView.getChildAt(if (reverseLayout) 0 else recyclerView.childCount.dec())
            position = recyclerView.getChildLayoutPosition(child)
            childBottom = child.bottom
            updateElevation(recyclerView)
        }

        fun updateElevation(recyclerView: RecyclerView?) {
            val elevation = when {
                reverseLayout && position != 0 -> maxElevation
                recyclerView == null -> maxElevation
                !reverseLayout && position != recyclerView.adapter!!.itemCount.dec() -> maxElevation
                else -> min(maxElevation, max(0f, childBottom - target.top - target.translationY))
            }
            materialShapeDrawable.elevation = elevation
        }
    }
}