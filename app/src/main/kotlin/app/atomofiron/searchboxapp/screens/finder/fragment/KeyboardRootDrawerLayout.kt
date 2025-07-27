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
import app.atomofiron.fileseeker.R
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

    private val focusListener = FocusChangeListener()
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
    private var itemView: View? = null
    private var editText: EditText? = null

    private var anim: ValueAnimator? = null
    // from the bottom
    private var keyboardNow = 0
    private var keyboardMax = resources.displayMetrics.heightPixels
    private var focusedBottom = 0

    init {
        setInsetsModifier { _, insets ->
            insets.builder()
                .consume(ExtType.ime)
                .build()
        }
        ViewCompat.setWindowInsetsAnimationCallback(this, callback)
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
        if (!::recyclerView.isInitialized) {
            recyclerView = child.findViewById(R.id.recycler_view)
            recyclerView.addOnChildAttachStateChangeListener(childListener)
        }
    }

    private fun updateAnyFocused(focusedView: View? = recyclerView.findFocus()) {
        focusedView?.onFocusChangeListener = focusListener
        var itemView = focusedView ?: return
        while (itemView.parent !is RecyclerView) {
            itemView = itemView.parent as View
        }
        val itemBottom = itemView.run { bottom + marginBottom }
        val newFocusedBottom = min(keyboardMax, recyclerView.height - itemBottom)
        anim?.cancel()
        if (keyboardNow <= min(focusedBottom, newFocusedBottom)) {
            // animation is unnecessary when new and old focused views are above the keyboard
            focusedBottom = newFocusedBottom
            return
        }
        if (newFocusedBottom == focusedBottom) {
            return
        }
        anim = ValueAnimator.ofInt(focusedBottom, newFocusedBottom).apply {
            duration = abs(newFocusedBottom - focusedBottom).toFloat()
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
                ignoring = false
                tracking = false
            }
            MotionEvent.ACTION_MOVE -> {
                tracker.addMovement(event)
                when {
                    ignoring -> Unit
                    tracking -> move(event)
                    event.x == prevX && event.y == prevY -> Unit
                    abs(event.x - prevX) >= abs(event.y - prevY) -> ignoring = true
                    start() -> {
                        event.action = MotionEvent.ACTION_CANCEL
                        super.dispatchTouchEvent(event)
                        move(event)
                    }
                    else -> ignoring = !tracking
                }
            }
            MotionEvent.ACTION_UP -> {
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
        val recyclerView = recyclerView
        val editText = editText ?: return false
        when {
            editText.isFocused -> Unit
            recyclerView.findFocus() != null -> Unit
            else -> editText.requestFocus()
        }
        if (!callback.visible) manager.showSoftInput(editText, 0)
        controlAnimation()
        delegate.resetAnimation()
        tracking = true
        return true
    }

    private fun move(event: MotionEvent) {
        val dy = event.y - prevY
        delegate.move(dy.roundToInt())
    }

    private fun updateTranslation() {
        recyclerView.translationY = -max(0, keyboardNow - focusedBottom).toFloat()
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
            itemView = holder.itemView
            editText = holder.itemView.findViewById(R.id.item_find_rt_find)
            editText?.onFocusChangeListener = focusListener
        }

        override fun onChildViewDetachedFromWindow(view: View) = Unit // LIER!
    }

    private inner class FocusChangeListener : OnFocusChangeListener {

        override fun onFocusChange(view: View, hasFocus: Boolean) {
            view.takeIf { hasFocus }
                .let { it ?: recyclerView.findFocus() }
                ?.let { updateAnyFocused(it) }
                ?: post { updateAnyFocused() }
        }
    }

    private inner class ValueListener : ValueAnimator.AnimatorUpdateListener {

        override fun onAnimationUpdate(animation: ValueAnimator) {
            focusedBottom = animation.animatedValue as Int
            updateTranslation()
        }
    }

    private inner class KeyboardListener : KeyboardInsetListener {

        override fun onImeStart(max: Int) {
            keyboardMax = max
            updateAnyFocused()
        }

        override fun onImeMove(current: Int) {
            keyboardNow = current
            updateTranslation()
        }

        override fun onImeEnd(visible: Boolean) {
            if (!visible) {
                recyclerView.findFocus()?.clearFocus()
            }
            isControlling = false
        }
    }
}