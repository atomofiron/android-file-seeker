package app.atomofiron.searchboxapp.custom

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntDef
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import app.atomofiron.searchboxapp.utils.disallowInterceptTouches
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class FastScroller(
    recyclerView: RecyclerView,
    private val mVerticalThumbDrawable: StateListDrawable,
    private val verticalTrackDrawable: Drawable,
    private val mHorizontalThumbDrawable: StateListDrawable,
    private val horizontalTrackDrawable: Drawable,
    defaultWidth: Int,
    private val mScrollbarMinimumRange: Int,
    private val areaSize: Int, // edit
    private val minThumbLength: Int = 0, // add
    private val inTheEnd: Boolean = true, // add
    private val requestRedraw: () -> Unit = { }, // add
) : ItemDecoration(), OnItemTouchListener {
    @IntDef(STATE_HIDDEN, STATE_VISIBLE, STATE_DRAGGING)
    @Retention(AnnotationRetention.SOURCE)
    private annotation class State

    @IntDef(DRAG_X, DRAG_Y, DRAG_NONE)
    @Retention(AnnotationRetention.SOURCE)
    private annotation class DragState

    @IntDef(ANIMATION_STATE_OUT, ANIMATION_STATE_FADING_IN, ANIMATION_STATE_IN, ANIMATION_STATE_FADING_OUT)
    @Retention(AnnotationRetention.SOURCE)
    private annotation class AnimationState

    private val mVerticalThumbWidth: Int = max(defaultWidth.toDouble(), mVerticalThumbDrawable.intrinsicWidth.toDouble()).toInt()
    private val mVerticalTrackWidth: Int = max(defaultWidth.toDouble(), verticalTrackDrawable.intrinsicWidth.toDouble()).toInt()

    private val mHorizontalThumbHeight: Int = max(defaultWidth.toDouble(), mHorizontalThumbDrawable.intrinsicWidth.toDouble()).toInt()
    private val mHorizontalTrackHeight: Int = max(defaultWidth.toDouble(), horizontalTrackDrawable.intrinsicWidth.toDouble()).toInt()

    // Dynamic values for the vertical scroll bar
    private var mVerticalThumbHeight = 0
    private var mVerticalThumbCenterY = 0
    private var mVerticalDragY = 0f

    // Dynamic values for the horizontal scroll bar
    private var mHorizontalThumbWidth = 0
    private var mHorizontalThumbCenterX = 0
    private var mHorizontalDragX = 0f

    private var mRecyclerViewWidth = 0
    private var mRecyclerViewHeight = 0
    // add
    private val horizontalArea get() = mRecyclerViewWidth - paddingLeft - paddingRight
    private val verticalArea get() = mRecyclerViewHeight - paddingTop - paddingBottom

    private var mRecyclerView: RecyclerView = recyclerView
    // add
    private val paddingTop get() = mRecyclerView.paddingTop
    private val paddingBottom get() = mRecyclerView.paddingBottom
    private val paddingLeft get() = mRecyclerView.paddingLeft
    private val paddingRight get() = mRecyclerView.paddingRight

    /**
     * Whether the document is long/wide enough to require scrolling. If not, we don't show the
     * relevant scroller.
     */
    private var mNeedVerticalScrollbar = false
    private var mNeedHorizontalScrollbar = false

    @State
    private var mState = STATE_HIDDEN

    @DragState
    private var mDragState = DRAG_NONE

    private val mVerticalRange = IntArray(2)
    private val mHorizontalRange = IntArray(2)
    private val mShowHideAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)

    @AnimationState
    private var mAnimationState: Int = ANIMATION_STATE_OUT
    private val mHideRunnable: Runnable = Runnable { hide(HIDE_DURATION_MS) }
    private val mOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            updateScrollPosition(recyclerView.computeHorizontalScrollOffset(), recyclerView.computeVerticalScrollOffset())
        }
    }

    // edit
    private val isLayoutRTL get() = (mRecyclerView.layoutDirection == View.LAYOUT_DIRECTION_RTL) xor !inTheEnd

    init {
        mVerticalThumbDrawable.alpha = SCROLLBAR_FULL_OPAQUE
        verticalTrackDrawable.alpha = SCROLLBAR_FULL_OPAQUE

        mShowHideAnimator.addListener(AnimatorListener())
        mShowHideAnimator.addUpdateListener(AnimatorUpdater())

        setupCallbacks() // edit
    }

    //edit
    fun attachToRecyclerView(recyclerView: RecyclerView) {
        if (mRecyclerView === recyclerView) {
            return
        }
        destroyCallbacks()
        mRecyclerView = recyclerView
        setupCallbacks()
    }

    private fun setupCallbacks() {
        mRecyclerView.addItemDecoration(this)
        mRecyclerView.addOnItemTouchListener(this)
        mRecyclerView.addOnScrollListener(mOnScrollListener)
    }

    private fun destroyCallbacks() {
        mRecyclerView.removeItemDecoration(this)
        mRecyclerView.removeOnItemTouchListener(this)
        mRecyclerView.removeOnScrollListener(mOnScrollListener)
        cancelHide()
    }

    fun requestRedraw() {
        mRecyclerView.invalidate()
        requestRedraw.invoke()
    }

    fun setState(@State state: Int) {
        if (state == STATE_DRAGGING && mState != STATE_DRAGGING) {
            mVerticalThumbDrawable.setState(PRESSED_STATE_SET)
            cancelHide()
        }

        when (state) {
            STATE_HIDDEN -> requestRedraw()
            else -> show()
        }

        if (mState == STATE_DRAGGING && state != STATE_DRAGGING) {
            mVerticalThumbDrawable.setState(EMPTY_STATE_SET)
            resetHideDelay(HIDE_DELAY_AFTER_DRAGGING_MS)
        } else if (state == STATE_VISIBLE) {
            resetHideDelay(HIDE_DELAY_AFTER_VISIBLE_MS)
        }
        mState = state
    }

    fun show() {
        when (mAnimationState) {
            ANIMATION_STATE_FADING_OUT -> {
                mShowHideAnimator.cancel()
                mAnimationState = ANIMATION_STATE_FADING_IN
                mShowHideAnimator.setFloatValues(mShowHideAnimator.animatedValue as Float, 1f)
                mShowHideAnimator.setDuration(SHOW_DURATION_MS.toLong())
                mShowHideAnimator.startDelay = 0
                mShowHideAnimator.start()
            }
            ANIMATION_STATE_OUT -> {
                mAnimationState = ANIMATION_STATE_FADING_IN
                mShowHideAnimator.setFloatValues(mShowHideAnimator.animatedValue as Float, 1f)
                mShowHideAnimator.setDuration(SHOW_DURATION_MS.toLong())
                mShowHideAnimator.startDelay = 0
                mShowHideAnimator.start()
            }
        }
    }

    private fun hide(duration: Int) {
        when (mAnimationState) {
            ANIMATION_STATE_FADING_IN -> {
                mShowHideAnimator.cancel()
                mAnimationState = ANIMATION_STATE_FADING_OUT
                mShowHideAnimator.setFloatValues(mShowHideAnimator.animatedValue as Float, 0f)
                mShowHideAnimator.setDuration(duration.toLong())
                mShowHideAnimator.start()
            }
            ANIMATION_STATE_IN -> {
                mAnimationState = ANIMATION_STATE_FADING_OUT
                mShowHideAnimator.setFloatValues(mShowHideAnimator.animatedValue as Float, 0f)
                mShowHideAnimator.setDuration(duration.toLong())
                mShowHideAnimator.start()
            }
        }
    }

    private fun cancelHide() {
        mRecyclerView.removeCallbacks(mHideRunnable)
    }

    private fun resetHideDelay(delay: Int) {
        cancelHide()
        mRecyclerView.postDelayed(mHideRunnable, delay.toLong())
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (mRecyclerViewWidth != mRecyclerView.width || mRecyclerViewHeight != mRecyclerView.height) {
            mRecyclerViewWidth = mRecyclerView.width
            mRecyclerViewHeight = mRecyclerView.height
            // This is due to the different events ordering when keyboard is opened or
            // retracted vs rotate. Hence to avoid corner cases we just disable the
            // scroller when size changed, and wait until the scroll position is recomputed
            // before showing it back.
            setState(STATE_HIDDEN)
            return
        }

        if (mAnimationState != ANIMATION_STATE_OUT) {
            if (mNeedVerticalScrollbar) {
                drawVerticalScrollbar(canvas)
            }
            if (mNeedHorizontalScrollbar) {
                drawHorizontalScrollbar(canvas)
            }
        }
    }

    private fun drawVerticalScrollbar(canvas: Canvas) {
        val viewWidth = mRecyclerViewWidth

        val left = viewWidth - mVerticalThumbWidth
        val top = mVerticalThumbCenterY - mVerticalThumbHeight / 2
        mVerticalThumbDrawable.setBounds(0, 0, mVerticalThumbWidth, mVerticalThumbHeight)
        verticalTrackDrawable.setBounds(0, paddingTop, mVerticalTrackWidth, paddingTop + verticalArea) // edit

        if (isLayoutRTL) {
            verticalTrackDrawable.draw(canvas)
            canvas.translate(mVerticalThumbWidth.toFloat(), top.toFloat())
            canvas.scale(-1f, 1f)
            mVerticalThumbDrawable.draw(canvas)
            canvas.scale(-1f, 1f)
            canvas.translate(-mVerticalThumbWidth.toFloat(), -top.toFloat())
        } else {
            canvas.translate(left.toFloat(), 0f)
            verticalTrackDrawable.draw(canvas)
            canvas.translate(0f, top.toFloat())
            mVerticalThumbDrawable.draw(canvas)
            canvas.translate(-left.toFloat(), -top.toFloat())
        }
    }

    private fun drawHorizontalScrollbar(canvas: Canvas) {
        val viewHeight = mRecyclerViewHeight

        val top = viewHeight - mHorizontalThumbHeight
        val left = mHorizontalThumbCenterX - mHorizontalThumbWidth / 2
        mHorizontalThumbDrawable.setBounds(0, 0, mHorizontalThumbWidth, mHorizontalThumbHeight)
        horizontalTrackDrawable.setBounds(paddingLeft, 0, paddingLeft + horizontalArea, mHorizontalTrackHeight) // edit

        canvas.translate(0f, top.toFloat())
        horizontalTrackDrawable.draw(canvas)
        canvas.translate(left.toFloat(), 0f)
        mHorizontalThumbDrawable.draw(canvas)
        canvas.translate(-left.toFloat(), -top.toFloat())
    }

    /**
     * Notify the scroller of external change of the scroll, e.g. through dragging or flinging on
     * the view itself.
     *
     * @param offsetX The new scroll X offset.
     * @param offsetY The new scroll Y offset.
     */
    fun updateScrollPosition(offsetX: Int, offsetY: Int) {
        val verticalContentLength = mRecyclerView.computeVerticalScrollRange()
        val verticalVisibleLength = verticalArea // edit
        mNeedVerticalScrollbar = (verticalContentLength - verticalVisibleLength > 0) && (verticalArea >= mScrollbarMinimumRange) // edit

        val horizontalContentLength = mRecyclerView.computeHorizontalScrollRange()
        val horizontalVisibleLength = horizontalArea // edit
        mNeedHorizontalScrollbar = (horizontalContentLength - horizontalVisibleLength > 0) && (horizontalArea >= mScrollbarMinimumRange) // edit

        if (!mNeedVerticalScrollbar && !mNeedHorizontalScrollbar) {
            if (mState != STATE_HIDDEN) {
                setState(STATE_HIDDEN)
            }
            return
        }
        if (mNeedVerticalScrollbar) {
            // edit val middleScreenPos = offsetY + verticalVisibleLength / 2.0f
            mVerticalThumbHeight = min(verticalVisibleLength, ((verticalVisibleLength * verticalVisibleLength) / verticalContentLength))
                .coerceAtLeast(minThumbLength) // add
            mVerticalThumbCenterY = paddingTop + mVerticalThumbHeight / 2 + // edit
                    ((verticalVisibleLength - mVerticalThumbHeight) * offsetY / (verticalContentLength - verticalArea)) // edit
        }
        if (mNeedHorizontalScrollbar) {
            // edit val middleScreenPos = offsetX + horizontalVisibleLength / 2.0f
            mHorizontalThumbWidth = min(horizontalVisibleLength, ((horizontalVisibleLength * horizontalVisibleLength) / horizontalContentLength))
                .coerceAtLeast(minThumbLength) // add
            mHorizontalThumbCenterX = paddingLeft + mHorizontalThumbWidth / 2 + // edit
                    ((horizontalVisibleLength * mHorizontalThumbWidth) * offsetX / (horizontalContentLength - horizontalArea)) // edit
        }
        if (mState == STATE_HIDDEN || mState == STATE_VISIBLE) {
            setState(STATE_VISIBLE)
        }
    }

    override fun onInterceptTouchEvent(recyclerView: RecyclerView, ev: MotionEvent): Boolean {
        return when (mState) {
            STATE_VISIBLE -> {
                val insideVerticalThumb = isPointInsideVerticalThumb(ev.x, ev.y)
                val insideHorizontalThumb = isPointInsideHorizontalThumb(ev.x, ev.y)
                if (ev.action == MotionEvent.ACTION_DOWN && (insideVerticalThumb || insideHorizontalThumb)) {
                    if (insideHorizontalThumb) {
                        mDragState = DRAG_X
                        mHorizontalDragX = ev.x.toInt().toFloat()
                    } else if (insideVerticalThumb) {
                        mDragState = DRAG_Y
                        mVerticalDragY = ev.y.toInt().toFloat()
                    }
                    setState(STATE_DRAGGING)
                    true
                } else {
                    false
                }
            }
            STATE_DRAGGING -> true
            else -> false
        }
    }

    override fun onTouchEvent(recyclerView: RecyclerView, me: MotionEvent) {
        if (mState == STATE_HIDDEN) {
            return
        }
        if (me.action == MotionEvent.ACTION_DOWN) {
            recyclerView.parent.disallowInterceptTouches() // edit
            val insideVerticalThumb = isPointInsideVerticalThumb(me.x, me.y)
            val insideHorizontalThumb = isPointInsideHorizontalThumb(me.x, me.y)
            if (insideVerticalThumb || insideHorizontalThumb) {
                if (insideHorizontalThumb) {
                    mDragState = DRAG_X
                    mHorizontalDragX = me.x.toInt().toFloat()
                } else if (insideVerticalThumb) {
                    mDragState = DRAG_Y
                    mVerticalDragY = me.y.toInt().toFloat()
                }
                setState(STATE_DRAGGING)
            }
        } else if (me.action == MotionEvent.ACTION_UP && mState == STATE_DRAGGING) {
            mVerticalDragY = 0f
            mHorizontalDragX = 0f
            setState(STATE_VISIBLE)
            mDragState = DRAG_NONE
            requestRedraw() // add
        } else if (me.action == MotionEvent.ACTION_MOVE && mState == STATE_DRAGGING) {
            show()
            if (mDragState == DRAG_X) {
                horizontalScrollTo(me.x)
            }
            if (mDragState == DRAG_Y) {
                verticalScrollTo(me.y)
            }
        }
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    private fun verticalScrollTo(y: Float) {
        val scrollbarRange = verticalRange
        // edit var y = max(scrollbarRange[0].toDouble(), min(scrollbarRange[1].toDouble(), y.toDouble())).toFloat()
        if (abs((mVerticalThumbCenterY - y).toDouble()) < 2) {
            return
        }
        val scrollingBy = scrollTo(
            mVerticalDragY, y, scrollbarRange,
            mRecyclerView.computeVerticalScrollRange(),
            mRecyclerView.computeVerticalScrollOffset(),
            verticalArea // edit
        )
        if (scrollingBy != 0) {
            mRecyclerView.scrollBy(0, scrollingBy)
        }
        mVerticalDragY = y
    }

    private fun horizontalScrollTo(x: Float) {
        val scrollbarRange = horizontalRange
        // edit var x = max(scrollbarRange[0].toDouble(), min(scrollbarRange[1].toDouble(), x.toDouble())).toFloat()
        if (abs((mHorizontalThumbCenterX - x).toDouble()) < 2) {
            return
        }
        val scrollingBy = scrollTo(
            mHorizontalDragX, x, scrollbarRange,
            mRecyclerView.computeHorizontalScrollRange(),
            mRecyclerView.computeHorizontalScrollOffset(),
            horizontalArea // edit
        )
        if (scrollingBy != 0) {
            mRecyclerView.scrollBy(scrollingBy, 0)
        }
        mHorizontalDragX = x
    }

    private fun scrollTo(oldDragPos: Float, newDragPos: Float, scrollbarRange: IntArray, scrollRange: Int, scrollOffset: Int, viewLength: Int): Int {
        val scrollbarLength = scrollbarRange[1] - scrollbarRange[0]
        if (scrollbarLength == 0) {
            return 0
        }
        val percentage = ((newDragPos - oldDragPos) / scrollbarLength.toFloat())
        val totalPossibleOffset = scrollRange - viewLength
        val scrollingBy = (percentage * totalPossibleOffset).toInt()
        val absoluteOffset = scrollOffset + scrollingBy
        return when (absoluteOffset) {
            in 0..<totalPossibleOffset -> scrollingBy
            else -> 0
        }
    }

    private fun isPointInsideVerticalThumb(x: Float, y: Float): Boolean {
        return when {
            isLayoutRTL -> x <= mVerticalThumbWidth.coerceAtLeast(areaSize) // edit
            else -> x >= mRecyclerViewWidth - mVerticalThumbWidth.coerceAtLeast(areaSize) // edit
        } && y >= mVerticalThumbCenterY - mVerticalThumbHeight / 2 && y <= mVerticalThumbCenterY + mVerticalThumbHeight / 2
    }

    private fun isPointInsideHorizontalThumb(x: Float, y: Float): Boolean {
        return (y >= mRecyclerViewHeight - mHorizontalThumbHeight.coerceAtLeast(areaSize)) // edit
                && x >= mHorizontalThumbCenterX - mHorizontalThumbWidth / 2 && x <= mHorizontalThumbCenterX + mHorizontalThumbWidth / 2
    }
    /**
     * Gets the (min, max) vertical positions of the vertical scroll bar.
     */
    private val verticalRange: IntArray get() {
        mVerticalRange[0] = paddingTop // edit
        mVerticalRange[1] = mRecyclerViewHeight - paddingBottom // edit
        return mVerticalRange
    }
    /**
     * Gets the (min, max) horizontal positions of the horizontal scroll bar.
     */
    private val horizontalRange: IntArray get() {
        mHorizontalRange[0] = paddingLeft // edit
        mHorizontalRange[1] = mRecyclerViewWidth - paddingRight // edit
        return mHorizontalRange
    }

    private inner class AnimatorListener : AnimatorListenerAdapter() {
        private var mCanceled = false

        override fun onAnimationEnd(animation: Animator) {
            // Cancel is always followed by a new directive, so don't update state.
            if (mCanceled) {
                mCanceled = false
                return
            }
            if (mShowHideAnimator.animatedValue == 0f) {
                mAnimationState = ANIMATION_STATE_OUT
                setState(STATE_HIDDEN)
            } else {
                mAnimationState = ANIMATION_STATE_IN
                requestRedraw()
            }
        }

        override fun onAnimationCancel(animation: Animator) {
            mCanceled = true
        }
    }

    private inner class AnimatorUpdater : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val alpha = (SCROLLBAR_FULL_OPAQUE * (valueAnimator.animatedValue as Float)).toInt()
            mVerticalThumbDrawable.alpha = alpha
            verticalTrackDrawable.alpha = alpha
            requestRedraw()
        }
    }

    companion object {
        // Scroll thumb not showing
        private const val STATE_HIDDEN = 0

        // Scroll thumb visible and moving along with the scrollbar
        private const val STATE_VISIBLE = 1

        // Scroll thumb being dragged by user
        private const val STATE_DRAGGING = 2

        private const val DRAG_NONE = 0
        private const val DRAG_X = 1
        private const val DRAG_Y = 2

        private const val ANIMATION_STATE_OUT = 0
        private const val ANIMATION_STATE_FADING_IN = 1
        private const val ANIMATION_STATE_IN = 2
        private const val ANIMATION_STATE_FADING_OUT = 3

        private const val SHOW_DURATION_MS = 500
        private const val HIDE_DELAY_AFTER_VISIBLE_MS = 1500
        private const val HIDE_DELAY_AFTER_DRAGGING_MS = 1200
        private const val HIDE_DURATION_MS = 500
        private const val SCROLLBAR_FULL_OPAQUE = 255

        private val PRESSED_STATE_SET = intArrayOf(android.R.attr.state_pressed)
        private val EMPTY_STATE_SET = intArrayOf()
    }
}
