package app.atomofiron.searchboxapp.screens.finder.fragment

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.DrawerStateListenerImpl
import app.atomofiron.common.util.UnreachableException
import app.atomofiron.common.util.extension.debugRequire
import app.atomofiron.common.util.hideKeyboard
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.layout.RootDrawerLayout
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.QueryFieldHolder
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.builder
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val AlphaThreshold = Alpha.SMALL
private const val HorizontalStart = 0f
private const val SpeedThreshold = 10
private const val VelocityPeriod = 100

const val DURATION = 256L

sealed interface Tracking {
    data object Undefined : Tracking
    data class Horizontal(val positive: Boolean) : Tracking
    data object Vertical : Tracking

    val horizontal get() = this is Horizontal
    val vertical get() = this == Vertical
    operator fun invoke(): Boolean = this != Undefined
}

class KeyboardRootDrawerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RootDrawerLayout(context, attrs, defStyleAttr) {

    private val drawerListener = DrawerStateListenerImpl()
    private val focusListener = FocusChangeListener()

    private var tracker = VelocityTracker.obtain()
    private var tracking: Tracking = Tracking.Undefined
    private var ignoring = false
    private var prevX = 0f
    private var prevY = 0f

    private val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    private lateinit var controller: WindowInsetsControllerCompat
    private val delegate = InsetsAnimator { focusedView != null }
    private val callback = KeyboardInsetCallback(KeyboardListener(), delegate.keyboardListener)
    private var isControlling = false // onReady is too slow

    private lateinit var recyclerView: RecyclerView
    private lateinit var contentView: ViewGroup
    private var editText: EditText? = null

    private var animVertical: ValueAnimator? = null
    private var animHorizontal: ValueAnimator? = null
    // from the bottom
    private var keyboardNow = 0
    private val keyboardMin = 0
    private var keyboardMax = resources.displayMetrics.heightPixels
    private var barrierBottom = 0
        set(value) {
            debugRequire(value <= keyboardMax)
            field = value
        }
    private var focusedView: View? = null
        get() {
            field = field
                ?.takeIf { it.isAttachedToWindow && it.isFocused }
                ?: recyclerView.findFocus()
            return field
        }
    private val horizontalCurrent get() = contentView.translationX
    private val horizontalMax get() = contentView.width.toFloat()
    private var rightToHide = true
    private var exitCallback: (() -> Unit)? = null

    init {
        setInsetsModifier { _, insets ->
            insets.builder()
                .consume(ExtType.ime)
                .build()
        }
        ViewCompat.setWindowInsetsAnimationCallback(this, callback)
        addDrawerListener(drawerListener)
    }

    fun setWindow(window: Window) {
        controller = WindowCompat.getInsetsController(window, window.decorView)
    }

    fun setCallback(onExit: () -> Unit) {
        exitCallback = onExit
    }

    fun animAppearing() {
        var from = horizontalMax.dec()
        if (!rightToHide) from *= -1
        animHorizontally(from, HorizontalStart)
    }

    fun animDisappearing() {
        val to = if (rightToHide) horizontalMax else -horizontalMax
        animHorizontally(horizontalCurrent, to)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        tracker.recycle()
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        child.findViewById<ViewGroup>(R.id.content)?.let {
            contentView = it
        }
        child.findViewById<RecyclerView>(R.id.recycler_view)?.let {
            recyclerView = it
            recyclerView.addOnChildAttachStateChangeListener(ChildStateListener())
            recyclerView.addOnScrollListener(ScrollListener())
        }
    }

    private fun updateAnyFocused() {
        focusedView?.let { updateAnyFocused(it) }
    }

    private fun updateAnyFocused(focusedView: View) {
        this.focusedView = focusedView
        focusedView.onFocusChangeListener = focusListener
        if (tracking()) {
            return
        }
        val itemBottom = focusedView.calcBottom() ?: return
        val newBarrierBottom = min(keyboardMax, itemBottom)
        animVertical?.cancel()
        if (keyboardNow <= min(barrierBottom, newBarrierBottom)) {
            // animation is unnecessary when new and old focused views are above the keyboard
            barrierBottom = newBarrierBottom
            return
        }
        if (newBarrierBottom == barrierBottom) {
            return
        }
        animVertical = ValueAnimator.ofInt(barrierBottom, newBarrierBottom).apply {
            duration = abs(newBarrierBottom - barrierBottom).toFloat()
                .let { DURATION * (it / keyboardMax) }.toLong()
            interpolator = DecelerateInterpolator()
            addUpdateListener(VerticalListener())
            start()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                tracker.addMovement(event)
                ignoring = drawerListener.isOpened
                tracking = Tracking.Undefined
                delegate.resetAnimation()
                animHorizontal?.cancel()
                animVertical?.cancel()
            }
            MotionEvent.ACTION_MOVE -> {
                tracker.addMovement(event)
                val dx = event.x - prevX
                val dy = event.y - prevY
                when {
                    ignoring -> Unit
                    tracking == Tracking.Vertical -> moveVertically(dy)
                    tracking is Tracking.Horizontal -> moveHorizontally(dx)
                    dx == 0f && dy == 0f -> Unit
                    abs(dx) > abs(dy) -> {
                        val positive = when {
                            horizontalCurrent > HorizontalStart -> true
                            horizontalCurrent < HorizontalStart -> false
                            else -> dx > 0
                        }
                        tracking = Tracking.Horizontal(positive)
                        focusedView?.hideKeyboard()
                        superCancelTouchEvents(event)
                    }
                    dy > 0 && keyboardNow == keyboardMin -> ignoring = true
                    dy < 0 && keyboardNow == keyboardMax -> {
                        tracking = Tracking.Vertical
                        moveVertically(dy)
                    }
                    startVertically() -> {
                        tracking = Tracking.Vertical
                        superCancelTouchEvents(event)
                        moveVertically(dy)
                    }
                    else -> ignoring = !tracking()
                }
            }
            MotionEvent.ACTION_UP -> {
                ignoring = false
                tracker.addMovement(event)
                tracker.computeCurrentVelocity(VelocityPeriod)
                val toRight = when {
                    tracking !is Tracking.Horizontal -> null
                    tracker.xVelocity < -SpeedThreshold -> false
                    tracker.xVelocity > SpeedThreshold -> true
                    else -> null
                }
                animHorizontally(toRight)
                val toShown = when (tracking) {
                    Tracking.Vertical -> when {
                        tracker.yVelocity < -SpeedThreshold -> true
                        tracker.yVelocity > SpeedThreshold -> false
                        else -> null
                    }
                    else -> false
                }
                when (true) {
                    (toShown == null),
                    (toShown && keyboardNow < keyboardMax),
                    (!toShown && keyboardNow != keyboardMin) -> delegate.start(toShown)
                    else -> Unit
                }
                if (tracking()) superCancelTouchEvents(event)
                tracking = Tracking.Undefined
                tracker.clear()
            }
        }
        prevX = event.x
        prevY = event.y
        if (!tracking()) super.dispatchTouchEvent(event)
        return true
    }

    private fun superCancelTouchEvents(event: MotionEvent) {
        event.action = MotionEvent.ACTION_CANCEL
        super.dispatchTouchEvent(event)
    }

    private fun startVertically(): Boolean {
        val editText = editText ?: return false
        when {
            editText.isFocused -> Unit
            focusedView != null -> Unit
            !editText.isFullyAboveBottom() -> return false
            else -> editText.requestFocus()
        }
        if (!callback.visible) manager.showSoftInput(editText, 0)
        ensureControlKeyboard()
        delegate.resetAnimation()
        return true
    }

    private fun moveVertically(dy: Float) {
        var offset = dy.roundToInt()
        if (offset == 0) {
            return
        }
        if (keyboardNow == keyboardMax && focusedView != null) {
            var newBarrier = barrierBottom + offset
            focusedView?.calcBottom()
                ?.let { newBarrier = min(newBarrier, it) }
            offset -= updateVertically(barrierBottom = newBarrier)
        }
        if (offset != 0) {
            ensureControlKeyboard()
            delegate.move(offset)
        }
    }

    private fun moveHorizontally(dx: Float) {
        val tracking = tracking as? Tracking.Horizontal ?: return
        var new = horizontalCurrent + dx
        if (tracking.positive != (new > 0)) {
            new = HorizontalStart
        }
        updateHorizontally(new)
    }

    private fun updateHorizontally(new: Float) {
        contentView.translationX = new
        val width = contentView.width.toFloat()
        alpha = (width - new.absoluteValue) / width + AlphaThreshold
        if (new.absoluteValue == width) {
            exitCallback?.invoke()
            rightToHide = new > 0
            post { updateHorizontally(HorizontalStart) }
        }
    }

    private fun animHorizontally(right: Boolean? = null) {
        val current = horizontalCurrent
        if (current == HorizontalStart) {
            return
        }
        val width = contentView.width.toFloat()
        val toRight = right ?: when {
            current > width / 2 -> true
            current > HorizontalStart -> false
            current > -width / 2 -> true
            else -> false
        }
        val target = when {
            toRight && current > HorizontalStart -> width
            toRight && current < HorizontalStart -> HorizontalStart
            !toRight && current > HorizontalStart -> HorizontalStart
            !toRight && current < HorizontalStart -> -width
            else -> throw UnreachableException()
        }
        animHorizontally(current, target)
    }

    private fun animHorizontally(from: Float, to: Float) {
        animHorizontal = ValueAnimator.ofFloat(from, to).apply {
            duration = (DURATION * abs(from - to) / horizontalMax).toLong()
            interpolator = DecelerateInterpolator()
            addUpdateListener(HorizontalListener())
            start()
        }
    }

    private fun View?.isFullyAboveBottom(): Boolean {
        this ?: return false
        val bottom = calcBottom() ?: return false
        return bottom >= recyclerView.paddingBottom
    }

    private fun View.calcBottom(): Int? {
        var itemView = this
        while (itemView.parent !is RecyclerView) {
            itemView = itemView.parent as? View
                ?: return null
        }
        return recyclerView.height - itemView.run { bottom + marginBottom }
    }

    private fun updateVertically(
        keyboardNow: Int = this.keyboardNow,
        barrierBottom: Int = this.barrierBottom,
    ): Int {
        this.keyboardNow = keyboardNow
        this.barrierBottom = barrierBottom.coerceIn(0, keyboardMax)
        val scrollNeeded = max(0, keyboardNow - this.barrierBottom)
        val scrollLeft = max(0, recyclerView.paddingBottom - this.barrierBottom)
        val scrolled = min(scrollNeeded, scrollLeft)
        if (scrolled != 0) recyclerView.scrollBy(0, scrolled)
        this.barrierBottom += scrolled
        val moveNeeded = -max(0, keyboardNow - this.barrierBottom)
        val moved = moveNeeded - recyclerView.translationY.toInt()
        recyclerView.translationY = moveNeeded.toFloat()
        return scrolled + moved
    }

    private fun ensureControlKeyboard() {
        if (!isControlling) {
            isControlling = true
            controller.controlWindowInsetsAnimation(Type.ime(), -1, null, null, delegate)
        }
    }

    private inner class ChildStateListener : RecyclerView.OnChildAttachStateChangeListener {

        override fun onChildViewAttachedToWindow(view: View) {
            val recyclerView = view.parent as RecyclerView
            val holder = recyclerView.getChildViewHolder(view) as? QueryFieldHolder
            holder ?: return
            editText = holder.etFind
            editText?.onFocusChangeListener = focusListener
        }

        override fun onChildViewDetachedFromWindow(view: View) = Unit // LIER!
    }

    private inner class FocusChangeListener : OnFocusChangeListener {

        override fun onFocusChange(view: View, hasFocus: Boolean) {
            view.takeIf { hasFocus }
                ?.let { updateAnyFocused(it) }
                ?: run {
                    updateAnyFocused()
                    post { updateAnyFocused() }
                }
        }
    }

    private inner class ScrollListener : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                updateAnyFocused()
            }
        }
    }

    private inner class VerticalListener : ValueAnimator.AnimatorUpdateListener {

        override fun onAnimationUpdate(animation: ValueAnimator) {
            updateVertically(barrierBottom = animation.animatedValue as Int)
        }
    }

    private inner class HorizontalListener : ValueAnimator.AnimatorUpdateListener {

        override fun onAnimationUpdate(animation: ValueAnimator) {
            updateHorizontally(animation.animatedValue as Float)
        }
    }

    private inner class KeyboardListener : KeyboardInsetListener {

        override fun onImeStart(max: Int) {
            keyboardMax = max
            updateAnyFocused()
        }

        override fun onImeMove(current: Int) {
            updateVertically(keyboardNow = current)
        }

        override fun onImeEnd(visible: Boolean) {
            if (!visible) focusedView?.clearFocus()
            isControlling = false
        }
    }
}