package app.atomofiron.searchboxapp.screens.finder.fragment

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Interpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.DrawerStateListenerImpl
import app.atomofiron.searchboxapp.custom.view.layout.RootDrawerLayout
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.QueryFieldHolder
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.builder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private const val HalfPi = PI.toFloat() / 2

class KeyboardRootDrawerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RootDrawerLayout(context, attrs, defStyleAttr) {

    private val drawerListener = DrawerStateListenerImpl()
    private val focusListener = FocusChangeListener()
    private val scrollListener = ScrollListener()
    private val childListener = ChildStateListener()
    private val valueListener = ValueListener()
    private val keyboardListener = KeyboardListener()
    private val sinusoid = Interpolator { input -> sin(HalfPi * input) }

    private var tracker = VelocityTracker.obtain()
    private var tracking = false
    private var ignoring = false
    private var prevX = 0f
    private var prevY = 0f

    private val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    private lateinit var controller: WindowInsetsControllerCompat
    private val delegate = InsetsAnimator { recyclerView.findFocus() != null }
    private val callback = KeyboardInsetCallback(keyboardListener, delegate.keyboardListener)
    private var isControlling = false // onReady is too slow

    private lateinit var recyclerView: RecyclerView
    private var editText: EditText? = null

    private var anim: ValueAnimator? = null
    // from the bottom
    private var keyboardNow = 0
    private val keyboardMin = 0
    private var keyboardMax = resources.displayMetrics.heightPixels
    private var barrierBottom = 0
        set(value) {
            if (value > keyboardMax) throw IllegalArgumentException(value.toString())
            field = value
        }
    private var focusedView: View? = null
        get() = field?.takeIf { it.isAttachedToWindow && it.isFocused }

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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        tracker.recycle()
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, index, params)
        if (child is RecyclerView) {
            recyclerView = child
            recyclerView.addOnChildAttachStateChangeListener(childListener)
            recyclerView.addOnScrollListener(scrollListener)
        }
    }

    private fun updateAnyFocused() {
        recyclerView.findFocus()?.let { updateAnyFocused(it) }
    }

    private fun updateAnyFocused(focusedView: View) {
        this.focusedView = focusedView
        focusedView.onFocusChangeListener = focusListener
        if (tracking) {
            return
        }
        val itemBottom = focusedView.calcBottom() ?: return
        val newBarrierBottom = min(keyboardMax, itemBottom)
        anim?.cancel()
        if (keyboardNow <= min(barrierBottom, newBarrierBottom)) {
            // animation is unnecessary when new and old focused views are above the keyboard
            barrierBottom = newBarrierBottom
            return
        }
        if (newBarrierBottom == barrierBottom) {
            return
        }
        anim = ValueAnimator.ofInt(barrierBottom, newBarrierBottom).apply {
            duration = abs(newBarrierBottom - barrierBottom).toFloat()
                .let { DURATION * (it / keyboardMax) }.toLong()
            interpolator = sinusoid
            addUpdateListener(valueListener)
            start()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                tracker.addMovement(event)
                ignoring = drawerListener.isOpened
                tracking = false
                delegate.resetAnimation()
            }
            MotionEvent.ACTION_MOVE -> {
                tracker.addMovement(event)
                val dx = event.x - prevX
                val dy = event.y - prevY
                when {
                    ignoring -> Unit
                    tracking -> move(dy)
                    dx == 0f && dy == 0f -> Unit
                    abs(dx) > abs(dy) -> ignoring = true
                    dy > 0 && keyboardNow == keyboardMin -> ignoring = true
                    dy < 0 && keyboardNow == keyboardMax -> {
                        tracking = true
                        move(dy)
                    }
                    start() -> {
                        tracking = true
                        event.action = MotionEvent.ACTION_CANCEL
                        super.dispatchTouchEvent(event)
                        move(dy)
                    }
                    else -> ignoring = !tracking
                }
            }
            MotionEvent.ACTION_UP -> {
                tracking = false
                ignoring = false
                tracker.addMovement(event)
                tracker.computeCurrentVelocity(100)
                val shown = when {
                    tracker.yVelocity < -10 -> true
                    tracker.yVelocity > 10 -> false
                    else -> null
                }
                delegate.start(shown)
                tracker.clear()
            }
        }
        prevX = event.x
        prevY = event.y
        if (!tracking) super.dispatchTouchEvent(event)
        return true
    }

    private fun start(): Boolean {
        val editText = editText ?: return false
        when {
            editText.isFocused -> Unit
            recyclerView.findFocus() != null -> Unit
            !editText.isFullyAboveBottom() -> return false
            else -> editText.requestFocus()
        }
        if (!callback.visible) manager.showSoftInput(editText, 0)
        controlAnimation()
        delegate.resetAnimation()
        return true
    }

    private fun move(dy: Float) {
        var offset = dy.roundToInt()
        if (offset == 0) {
            return
        }
        offset -= if (keyboardNow == keyboardMax && focusedView != null) {
            val min = recyclerView.run { height - getChildAt(0).let { it.bottom + it.marginBottom } }
            val max = focusedView?.calcBottom() ?: keyboardMax
            var newBarrier = max(barrierBottom + offset, min)
            newBarrier = min(newBarrier, max)
            updateTranslation(barrierBottom = newBarrier)
        } else {
            0
        }
        if (offset != 0) {
            controlAnimation()
            delegate.move(offset)
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

    private fun updateTranslation(
        keyboardNow: Int = this.keyboardNow,
        barrierBottom: Int = this.barrierBottom,
    ): Int {
        this.keyboardNow = keyboardNow
        this.barrierBottom = barrierBottom.coerceIn(0, keyboardMax)
        val new = -max(0, keyboardNow - this.barrierBottom)
        val moved = new - recyclerView.translationY.toInt()
        recyclerView.translationY = new.toFloat()
        return moved
    }

    private fun controlAnimation() {
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

    private inner class ValueListener : ValueAnimator.AnimatorUpdateListener {

        override fun onAnimationUpdate(animation: ValueAnimator) {
            updateTranslation(barrierBottom = animation.animatedValue as Int)
        }
    }

    private inner class KeyboardListener : KeyboardInsetListener {

        override fun onImeStart(max: Int) {
            keyboardMax = max
            updateAnyFocused()
        }

        override fun onImeMove(current: Int) {
            updateTranslation(keyboardNow = current)
        }

        override fun onImeEnd(visible: Boolean) {
            if (!visible) {
                recyclerView.findFocus()?.clearFocus()
            }
            isControlling = false
        }
    }
}