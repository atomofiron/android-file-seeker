package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import app.atomofiron.common.util.extension.findAs
import app.atomofiron.common.util.extension.hasBits
import app.atomofiron.common.util.isDarkDeep
import app.atomofiron.common.util.noClip
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.drawable.tonedOverlay
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.inflater
import app.atomofiron.searchboxapp.utils.invoke
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_OFF
import com.google.android.material.appbar.CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
import com.google.android.material.shape.MaterialShapeDrawable
import kotlin.math.min
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED as EUC
import com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL as SCR

class HeaderLayout : AppBarLayout, AppBarLayout.OnOffsetChangedListener {

    private val behavior = HeaderBehavior()
    private val collapsing: CollapsingToolbarLayout
    private var subBar: View? = null
    private var underBar: View? = null
    private var toolbar: View? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        attrs?.apply()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        attrs?.apply(defStyleAttr)
    }

    init {
        inflater().inflate(R.layout.view_header_layout, this)
        collapsing = findViewById(R.id.collapsing)
        pinToolbar(true)
        addOnOffsetChangedListener(this)
        noClip()
        collapsing.noClip()
        if (context.isDarkDeep()) {
            addLiftOnScrollListener { _, color ->
                val background = background as MaterialShapeDrawable
                background.fillColor = context.tonedOverlay(color)
            }
        }
    }

    private fun AttributeSet.apply(defStyleAttr: Int = 0) {
        context.withStyledAttributes(this, R.styleable.HeaderLayout, defStyleAttr, 0) {
            val resId = getResourceId(R.styleable.HeaderLayout_headerExpandedHeight, 0)
            if (resId != 0) {
                collapsing.updateLayoutParams {
                    height = resources.getDimensionPixelSize(resId)
                }
            }
            collapsing.isTitleEnabled = collapsing.layoutParams.height > 0
            if (!collapsing.isTitleEnabled) {
                collapsing.contentScrim = null
            }
        }
    }

    fun pinToolbar(pin: Boolean) {
        isLiftOnScroll = pin
        collapsing<LayoutParams> {
            scrollFlags = if (pin) SCR or EUC else SCR
        }
    }

    fun isExpanded(): Boolean = behavior.topAndBottomOffset == 0

    override fun getBehavior(): CoordinatorLayout.Behavior<AppBarLayout?> = behavior

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        updateToolbarAlpha(verticalOffset)
        updateSubBarAlpha(verticalOffset)
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child.id == R.id.collapsing) {
            return super.addView(child, index, params)
        }
        if (child is Toolbar) {
            if (toolbar != null) throw IllegalArgumentException()
            toolbar = child
            val params = CollapsingToolbarLayout.LayoutParams(params)
            params.collapseMode = COLLAPSE_MODE_PIN
            collapsing.addView(child, collapsing.childCount, params)
            child.doOnLayout {
                subBar?.updateLayoutParams<MarginLayoutParams> {
                    topMargin = child.height
                }
            }
        } else if (subBar == null) {
            subBar = child
            val params = CollapsingToolbarLayout.LayoutParams(params)
            params.collapseMode = COLLAPSE_MODE_OFF
            collapsing.addView(child, 0, params)
            child.doOnLayout {
                behavior.limitOffset = -it.height
            }
        } else if (underBar == null) {
            underBar = child
            super.addView(child, index - collapsing.childCount, params)
        } else {
            throw IllegalArgumentException()
        }
    }

    override fun setExpanded(expanded: Boolean, animated: Boolean) {
        behavior.limitOffset = 0
        super.setExpanded(expanded, animated)
    }

    private fun updateToolbarAlpha(offset: Int) {
        val toolbar = toolbar ?: return
        val flags = collapsing<LayoutParams>().scrollFlags
        toolbar.alpha = if (flags.hasBits(SCR) && !flags.hasBits(EUC)) {
            val toolbarHeight = toolbar.height
            val subHeight = subBar?.height ?: 0
            val alpha = (toolbarHeight + subHeight + offset) / toolbarHeight.toFloat()
            Alpha.halfInvisible(alpha)
        } else {
            Alpha.VISIBLE
        }
    }

    private fun updateSubBarAlpha(offset: Int) {
        val subBar = subBar ?: return
        val subHeight = subBar.height
        val alpha = (subHeight + offset) / subHeight.toFloat()
        subBar.alpha = Alpha.halfInvisible(alpha)
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
