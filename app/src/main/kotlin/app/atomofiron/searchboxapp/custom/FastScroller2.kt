package app.atomofiron.searchboxapp.custom

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.R
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntDef
import androidx.annotation.VisibleForTesting
import androidx.collection.MutableIntList
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Class responsible to animate and provide a fast scroller.
 */
class FastScroller2(
    recyclerView: RecyclerView,
    @get:VisibleForTesting val mVerticalThumbDrawable: StateListDrawable,
    @get:VisibleForTesting val mVerticalTrackDrawable: Drawable,
    private val mHorizontalThumbDrawable: StateListDrawable,
    private val mHorizontalTrackDrawable: Drawable,
    thickness: Int = 0,
    private val minThumbThickness: Int = recyclerView.resources.displayMetrics.density.roundToInt(),
    private val mScrollbarMinimumRange: Int,
    minDragAreaSize: Int = thickness,
    private val minThumbLength: Int = 0,
    private val inTheEnd: Boolean = true,
    private val callback: ((Action) -> Unit)? = null,
) : ItemDecoration(), OnItemTouchListener {
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

        private const val PIXEL_HALF = 0.5f

        private val PRESSED_STATE_SET = intArrayOf(android.R.attr.state_pressed)
        private val EMPTY_STATE_SET = intArrayOf()
    }

    enum class Action {
        Redraw, DragStart
    }

    @IntDef(STATE_HIDDEN, STATE_VISIBLE, STATE_DRAGGING)
    @Retention(AnnotationRetention.SOURCE)
    private annotation class State

    @IntDef(DRAG_X, DRAG_Y, DRAG_NONE)
    @Retention(AnnotationRetention.SOURCE)
    private annotation class DragState

    @IntDef(ANIMATION_STATE_OUT, ANIMATION_STATE_FADING_IN, ANIMATION_STATE_IN, ANIMATION_STATE_FADING_OUT)
    @Retention(AnnotationRetention.SOURCE)
    private annotation class AnimationState

    private val mVerticalThumbWidth: Int = max(thickness, mVerticalThumbDrawable.intrinsicWidth)
    private val mVerticalTrackWidth: Int = max(thickness, mVerticalTrackDrawable.intrinsicWidth)

    private val mHorizontalThumbHeight: Int = max(thickness, mHorizontalThumbDrawable.intrinsicWidth)
    private val mHorizontalTrackHeight: Int = max(thickness, mHorizontalTrackDrawable.intrinsicWidth)

    private val verticalThumbArea = max(minDragAreaSize, mVerticalThumbWidth)
    private val horizontalThumbArea = max(minDragAreaSize, mHorizontalThumbHeight)

    // Dynamic values for the vertical scroll bar
    @VisibleForTesting
    var mVerticalThumbHeight = 0
    @VisibleForTesting
    var mVerticalThumbCenterY = 0
        set(value) {
            avgSpeedY.add(abs(value - field))
            field = value
        }
    @VisibleForTesting
    var mVerticalDownY = 0f
    @VisibleForTesting
    var mVerticalDownOffset = 0

    // Dynamic values for the horizontal scroll bar
    @VisibleForTesting
    var mHorizontalThumbWidth = 0
    @VisibleForTesting
    var mHorizontalThumbCenterX = 0
        set(value) {
            avgSpeedX.add(abs(value - field))
            field = value
        }
    @VisibleForTesting
    var mHorizontalDownX = 0f
    @VisibleForTesting
    var mHorizontalDownOffset = 0

    private val avgSpeedX = AvgSpeed()
    private val avgSpeedY = AvgSpeed()

    private var mRecyclerViewWidth = 0
    private var mRecyclerViewHeight = 0

    private var mRecyclerView: RecyclerView = recyclerView

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

    private val mShowHideAnimator = ValueAnimator.ofFloat(0f, 1f)

    @AnimationState
    private var mAnimationState = ANIMATION_STATE_OUT
    private val mHideRunnable = Runnable { hide(HIDE_DURATION_MS) }
    private val mOnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dx != 0 || dy != 0) {
                updateScrollPosition(recyclerView.computeHorizontalScrollOffset(), recyclerView.computeVerticalScrollOffset())
            }
        }
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                resetSpeed()
            }
        }
    }

    private val onTheLeft get() = (mRecyclerView.layoutDirection == View.LAYOUT_DIRECTION_RTL) == inTheEnd
    private var isShowingLayoutBounds = false
    private val debugPaint by lazy(LazyThreadSafetyMode.NONE) {
        Paint().apply {
            style = Paint.Style.STROKE
            color = Color.RED
            strokeWidth = 1f
        }
    }

    init {
        mVerticalThumbDrawable.alpha = SCROLLBAR_FULL_OPAQUE
        mVerticalTrackDrawable.alpha = SCROLLBAR_FULL_OPAQUE

        mShowHideAnimator.addListener(AnimatorListener())
        mShowHideAnimator.addUpdateListener(AnimatorUpdater())

        setupCallbacks()
    }

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
        callback?.invoke(Action.Redraw)
    }

    fun setState(@State state: Int) {
        if (state == STATE_DRAGGING && mState != STATE_DRAGGING) {
            mVerticalThumbDrawable.setState(PRESSED_STATE_SET)
            cancelHide()
            callback?.invoke(Action.DragStart)
        }
        if (state != mState) {
            requestRedraw()
        }
        if (state != STATE_HIDDEN) {
            show()
        }
        if (mState == STATE_DRAGGING && state != STATE_DRAGGING) {
            mVerticalThumbDrawable.setState(EMPTY_STATE_SET)
            resetHideDelay(HIDE_DELAY_AFTER_DRAGGING_MS)
            resetSpeed()
        } else if (state == STATE_VISIBLE) {
            resetHideDelay(HIDE_DELAY_AFTER_VISIBLE_MS)
        }
        mState = state
    }

    fun isDragging() = mState == STATE_DRAGGING

    @VisibleForTesting
    fun isVisible() = mState == STATE_VISIBLE

    private fun show() {
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
        isShowingLayoutBounds = if (SDK_INT >= R) mRecyclerView.isShowingLayoutBounds else false
    }

    @VisibleForTesting
    fun hide(duration: Int) {
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
        val speed = avgSpeedY.major(mDragState)
        mVerticalThumbDrawable.setBounds(0, 0, max(minThumbThickness, mVerticalThumbWidth - speed), mVerticalThumbHeight)
        mVerticalTrackDrawable.setBounds(0, paddingTop, mVerticalTrackWidth, paddingTop + getVerticalTrackArea())

        val saved = canvas.save()
        if (onTheLeft) {
            canvas.translate(paddingLeft - avgSpeedY.minor(mDragState), 0f)
        } else {
            canvas.translate(mRecyclerViewWidth - paddingRight + avgSpeedY.minor(mDragState), 0f)
            canvas.scale(-1f, 1f)
        }
        mVerticalTrackDrawable.draw(canvas)
        canvas.translate(0f, mVerticalThumbCenterY - mVerticalThumbHeight / 2f)
        mVerticalThumbDrawable.draw(canvas)
        if (isShowingLayoutBounds) {
            canvas.drawRect(PIXEL_HALF, 0f, verticalThumbArea - PIXEL_HALF, mVerticalThumbHeight.toFloat(), debugPaint)
        }
        canvas.restoreToCount(saved)
    }

    private fun drawHorizontalScrollbar(canvas: Canvas) {
        val left = mHorizontalThumbCenterX - mHorizontalThumbWidth / 2f
        val speed = if (mDragState == STATE_DRAGGING) 0 else avgSpeedX.major(mDragState)
        mHorizontalThumbDrawable.setBounds(0, 0, mHorizontalThumbWidth, max(minThumbThickness, mHorizontalThumbHeight - speed))
        mHorizontalTrackDrawable.setBounds(paddingLeft, 0, paddingLeft + getHorizontalTrackArea(), mHorizontalTrackHeight)

        val saved = canvas.save()
        canvas.translate(0f, mRecyclerViewHeight - paddingBottom + avgSpeedX.minor(mDragState))
        canvas.scale(1f, -1f)
        mHorizontalTrackDrawable.draw(canvas)
        canvas.translate(left, 0f)
        mHorizontalThumbDrawable.draw(canvas)
        if (isShowingLayoutBounds) {
            canvas.drawRect(0f, PIXEL_HALF, mHorizontalThumbWidth.toFloat(), horizontalThumbArea - PIXEL_HALF, debugPaint)
        }
        canvas.restoreToCount(saved)
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
        val verticalVisibleLength = getVerticalTrackArea()
        mNeedVerticalScrollbar = verticalContentLength - verticalVisibleLength >= mScrollbarMinimumRange

        val horizontalContentLength = mRecyclerView.computeHorizontalScrollRange()
        val horizontalVisibleLength = getHorizontalTrackArea()
        mNeedHorizontalScrollbar = horizontalContentLength - horizontalVisibleLength >= mScrollbarMinimumRange

        if (!mNeedVerticalScrollbar && !mNeedHorizontalScrollbar) {
            if (mState != STATE_HIDDEN) {
                setState(STATE_HIDDEN)
            }
            return
        }
        if (mNeedVerticalScrollbar) {
            mVerticalThumbHeight = min(verticalVisibleLength, ((verticalVisibleLength * verticalVisibleLength) / verticalContentLength))
                .coerceAtLeast(minThumbLength)
            mVerticalThumbCenterY = paddingTop + mVerticalThumbHeight / 2 +
                    ((verticalVisibleLength - mVerticalThumbHeight) * offsetY / (verticalContentLength - verticalVisibleLength))
        }
        if (mNeedHorizontalScrollbar) {
            mHorizontalThumbWidth = min(horizontalVisibleLength, ((horizontalVisibleLength * horizontalVisibleLength) / horizontalContentLength))
                .coerceAtLeast(minThumbLength)
            mHorizontalThumbCenterX = paddingLeft + mHorizontalThumbWidth / 2 +
                    ((horizontalVisibleLength * mHorizontalThumbWidth) * offsetX / (horizontalContentLength - horizontalVisibleLength))
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
                        mHorizontalDownX = ev.x
                        mHorizontalDownOffset = mRecyclerView.computeHorizontalScrollOffset()
                    } else {
                        mDragState = DRAG_Y
                        mVerticalDownY = ev.y
                        mVerticalDownOffset = mRecyclerView.computeVerticalScrollOffset()
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
            val insideVerticalThumb = isPointInsideVerticalThumb(me.x, me.y)
            val insideHorizontalThumb = isPointInsideHorizontalThumb(me.x, me.y)
            if (insideVerticalThumb || insideHorizontalThumb) {
                if (insideHorizontalThumb) {
                    mDragState = DRAG_X
                    mHorizontalDownX = me.x
                    mHorizontalDownOffset = mRecyclerView.computeHorizontalScrollOffset()
                } else {
                    mDragState = DRAG_Y
                    mVerticalDownY = me.y
                    mVerticalDownOffset = mRecyclerView.computeVerticalScrollOffset()
                }
                setState(STATE_DRAGGING)
            }
        } else if (me.action == MotionEvent.ACTION_UP && mState == STATE_DRAGGING) {
            setState(STATE_VISIBLE)
            mDragState = DRAG_NONE
        } else if (me.action == MotionEvent.ACTION_MOVE && mState == STATE_DRAGGING) {
            show()
            when (mDragState) {
                DRAG_X -> horizontalScrollTo(me.x)
                DRAG_Y -> verticalScrollTo(me.y)
            }
        }
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    private fun verticalScrollTo(y: Float) {
        if (abs(mVerticalThumbCenterY - y) < 2) {
            return
        }
        val scrollingBy = scrollTo(
            mVerticalDownY, y,
            mRecyclerView.computeVerticalScrollRange(),
            mRecyclerView.computeVerticalScrollOffset(),
            mVerticalDownOffset,
            getVerticalTrackArea(),
            mVerticalThumbHeight,
        )
        if (scrollingBy != 0) {
            mRecyclerView.scrollBy(0, scrollingBy)
        }
    }

    private fun horizontalScrollTo(x: Float) {
        if (abs((mHorizontalThumbCenterX - x)) < 2) {
            return
        }
        val scrollingBy = scrollTo(
            mHorizontalDownX, x,
            mRecyclerView.computeHorizontalScrollRange(),
            mRecyclerView.computeHorizontalScrollOffset(),
            mHorizontalDownOffset,
            getHorizontalTrackArea(),
            mHorizontalThumbWidth,
        )
        if (scrollingBy != 0) {
            mRecyclerView.scrollBy(scrollingBy, 0)
        }
    }

    private fun scrollTo(
        downDragPos: Float,
        newDragPos: Float,
        scrollRange: Int,
        scrollOffset: Int,
        downScrollOffset: Int,
        areaLength: Int,
        thumbLength: Int,
    ): Int {
        if (areaLength == 0) {
            return 0
        }
        val percentage = (newDragPos - downDragPos) / (areaLength - thumbLength)
        val totalPossibleOffset = scrollRange - areaLength
        val absoluteOffset = downScrollOffset + (percentage * totalPossibleOffset).toInt()
        var scrollingBy = absoluteOffset - scrollOffset
        when {
            absoluteOffset < 0 -> scrollingBy += absoluteOffset
            absoluteOffset > totalPossibleOffset -> scrollingBy += absoluteOffset - totalPossibleOffset
        }
        return scrollingBy
    }

    @VisibleForTesting
    fun isPointInsideVerticalThumb(x: Float, y: Float): Boolean {
        return when {
            onTheLeft -> x <= verticalThumbArea
            else -> x >= mRecyclerViewWidth - verticalThumbArea
        } && y >= mVerticalThumbCenterY - mVerticalThumbHeight / 2 && y <= mVerticalThumbCenterY + mVerticalThumbHeight / 2
    }

    @VisibleForTesting
    fun isPointInsideHorizontalThumb(x: Float, y: Float): Boolean {
        return (y >= mRecyclerViewHeight - horizontalThumbArea)
                && x >= mHorizontalThumbCenterX - mHorizontalThumbWidth / 2 && x <= mHorizontalThumbCenterX + mHorizontalThumbWidth / 2
    }

    /**
     * Gets the vertical area of the vertical scroll bar.
     */
    @VisibleForTesting
    private fun getVerticalTrackArea() = mRecyclerViewHeight - paddingTop - paddingBottom

    /**
     * Gets the horizontal area of the horizontal scroll bar.
     */
    private fun getHorizontalTrackArea() = mRecyclerViewWidth - paddingLeft - paddingRight

    private fun resetSpeed() {
        avgSpeedX.clear()
        avgSpeedY.clear()
        requestRedraw()
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
            mVerticalTrackDrawable.alpha = alpha
            requestRedraw()
        }
    }

    private class AvgSpeed {

        private val list = MutableIntList(8)

        fun add(speed: Int) {
            if (list.size == 8) {
                list.removeAt(0)
            }
            list.add(speed / 1)
        }

        private fun general() = if (list.isEmpty()) 0f else (list.sum() / list.size.toFloat())

        fun major(@DragState state: Int) = if (state == STATE_DRAGGING) 0 else general().toInt()

        fun minor(@DragState state: Int) = if (state == STATE_DRAGGING) 0f else (general() % 1f)

        fun clear() = list.clear()

        private fun MutableIntList.sum(): Int {
            var sum = 0
            for (i in indices) sum += get(i)
            return sum
        }
    }
}
