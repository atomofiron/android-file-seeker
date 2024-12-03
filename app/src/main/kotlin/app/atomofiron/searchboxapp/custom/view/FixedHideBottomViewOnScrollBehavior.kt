package app.atomofiron.searchboxapp.custom.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewPropertyAnimator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.animation.AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR
import com.google.android.material.animation.AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR

open class FixedHideBottomViewOnScrollBehavior @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : CoordinatorLayout.Behavior<View>(context, attrs) {
    companion object {
        protected const val ENTER_ANIMATION_DURATION = 225L
        protected const val EXIT_ANIMATION_DURATION = 175L
        const val STATE_SCROLLED_OFF = 1
        const val STATE_SCROLLED_START = 2
    }

    private var height = 0
    private var currentState = STATE_SCROLLED_START
    private var additionalHiddenOffsetY = 0

    private var currentAnimator: ViewPropertyAnimator? = null
    private var animatorListener = AnimatorListenerAdapterImpl()

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {
        val paramsCompat = child.layoutParams as MarginLayoutParams
        height = child.measuredHeight + paramsCompat.bottomMargin
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onStartNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: View,
            directTargetChild: View,
            target: View,
            nestedScrollAxes: Int,
            type: Int): Boolean {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    @Suppress("NAME_SHADOWING")
    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        if (dyConsumed == 0 && dyUnconsumed == 0) {
            return
        }
        var dyConsumed = dyConsumed
        var dyUnconsumed = dyUnconsumed
        val isTargetReversed = isTargetReversed(target)
        if (isTargetReversed) {
            dyConsumed *= -1
            dyUnconsumed *= -1
        }
        when {
            !isTargetReversed && dyUnconsumed != 0 -> slideStart(child)
            dyUnconsumed < 0 -> slideStart(child)
            dyConsumed < 0 -> slideStart(child)
            dyConsumed > 0 -> slideOff(child)
        }
    }

    /**
     * Perform an animation that will slide the child
     * from it's current position to be totally on the screen.
     */
    private fun slideStart(child: View) {
        if (currentState == STATE_SCROLLED_START) {
            return
        }
        if (currentAnimator != null) {
            currentAnimator!!.cancel()
            child.clearAnimation()
        }
        currentState = STATE_SCROLLED_START
        animateChildTo(child, 0, ENTER_ANIMATION_DURATION, LINEAR_OUT_SLOW_IN_INTERPOLATOR)
    }

    /**
     * Perform an animation that will slide the child
     * from it's current position to be totally off the screen.
     */
    private fun slideOff(child: View) {
        if (currentState == STATE_SCROLLED_OFF) {
            return
        }
        if (currentAnimator != null) {
            currentAnimator!!.cancel()
            child.clearAnimation()
        }
        currentState = STATE_SCROLLED_OFF
        val targetY = height + additionalHiddenOffsetY
        animateChildTo(child, targetY, EXIT_ANIMATION_DURATION, FAST_OUT_LINEAR_IN_INTERPOLATOR)
    }

    private fun animateChildTo(child: View, targetY: Int, duration: Long, interpolator: TimeInterpolator) {
        currentAnimator = child
                .animate()
                .translationY(targetY.toFloat())
                .setInterpolator(interpolator)
                .setDuration(duration)
                .setListener(animatorListener)
    }

    private fun isTargetReversed(target: View): Boolean {
        if (target is RecyclerView) {
            val layoutManager = target.layoutManager
            if (layoutManager is LinearLayoutManager) {
                return layoutManager.reverseLayout
            }
        }
        return false
    }

    private inner class AnimatorListenerAdapterImpl : AnimatorListenerAdapter() {
        override fun onAnimationCancel(animation: Animator) = animation.removeListener(this)

        override fun onAnimationEnd(animation: Animator) {
            currentAnimator = null
            animation.removeListener(this)
        }
    }
}