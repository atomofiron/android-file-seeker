package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.core.view.doOnNextLayout
import androidx.core.view.updateLayoutParams
import app.atomofiron.common.util.extension.findAs
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.utils.inflater
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_OFF
import com.google.android.material.appbar.CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
import kotlin.math.min

class HeaderLayout : AppBarLayout, AppBarLayout.OnOffsetChangedListener {

    private val behavior = HeaderBehavior()
    private val collapsing: CollapsingToolbarLayout
    private var subBar: View? = null
    private var toolbar: View? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        inflater().inflate(R.layout.view_header_layout, this)
        collapsing = findViewById(R.id.collapsing)
        addOnOffsetChangedListener(this)
    }

    fun pinToolbar(pin: Boolean) {
        isLiftOnScroll = pin
        collapsing.updateLayoutParams<LayoutParams> {
            scrollFlags = when {
                pin -> SCROLL_FLAG_SCROLL or SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
                else -> SCROLL_FLAG_SCROLL
            }
        }
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<AppBarLayout?> = behavior

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        val toolbarHeight = toolbar?.height ?: return
        val subHeight = subBar?.height ?: 0
        alpha = (toolbarHeight + subHeight + verticalOffset) / toolbarHeight.toFloat()
        subBar?.alpha = (subHeight + verticalOffset) / subHeight.toFloat()
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child.id == R.id.collapsing) {
            return super.addView(child, index, params)
        }
        val params = CollapsingToolbarLayout.LayoutParams(params)
        if (child is Toolbar) {
            if (toolbar != null) throw IllegalArgumentException()
            toolbar = child
            params.collapseMode = COLLAPSE_MODE_PIN
            collapsing.addView(child, collapsing.childCount, params)
            child.doOnNextLayout {
                subBar?.updateLayoutParams<MarginLayoutParams> {
                    topMargin = child.height
                }
            }
        } else {
            if (subBar != null) throw IllegalArgumentException()
            subBar = child
            params.collapseMode = COLLAPSE_MODE_OFF
            collapsing.addView(child, 0, params)
            child.doOnNextLayout {
                behavior.limitOffset = -it.height
            }
        }
    }

    private class HeaderBehavior : Behavior() {

        private var start = false
        private var skip = false
        var limitOffset = 0

        override fun setTopAndBottomOffset(offset: Int): Boolean {
            return super.setTopAndBottomOffset(min(limitOffset, offset))
        }

        override fun onStartNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: AppBarLayout,
            directTargetChild: View,
            target: View,
            axes: Int,
            type: Int,
        ): Boolean {
            start = true
            return !skip || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
        }

        override fun onNestedPreScroll(
            coordinatorLayout: CoordinatorLayout,
            child: AppBarLayout,
            target: View,
            dx: Int,
            dy: Int,
            consumed: IntArray,
            type: Int,
        ) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
            val subBar = coordinatorLayout.children
                .findAs<HeaderLayout>()
                ?.subBar
            when {
                subBar == null -> Unit
                target.canScrollVertically(-1) -> limitOffset = -subBar.height
                start && !skip && dy < 0 -> limitOffset = 0
            }
            start = false
            skip = false
        }

        override fun onNestedPreFling(
            coordinatorLayout: CoordinatorLayout,
            child: AppBarLayout,
            target: View,
            velocityX: Float,
            velocityY: Float,
        ): Boolean {
            skip = true
            return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
        }
    }
}
