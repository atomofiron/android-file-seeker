package app.atomofiron.searchboxapp.screens.finder.fragment

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.layout.RootDrawerLayout
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.QueryFieldHolder
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.builder
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

class KeyboardRootDrawerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RootDrawerLayout(context, attrs, defStyleAttr), RecyclerView.OnChildAttachStateChangeListener {

    private var tracker = VelocityTracker.obtain()
    private var tracking = false
    private var ignoring = false
    private var prevX = 0f
    private var prevY = 0f

    private val manager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    private lateinit var controller: WindowInsetsControllerCompat
    private val delegate = InsetsDelegate(::onIme)
    private var recyclerView: RecyclerView? = null
    private var itemView: View? = null
    private var editText: EditText? = null
    private var bottomInset = 0
    private val bottomTypes = ExtType { navigationBars + displayCutout + joystickBottom }
    private var imeIsVisible = false

    init {
        setInsetsModifier { _, insets ->
            bottomInset = insets[bottomTypes].bottom
            insets.builder()
                .consume(ExtType.ime)
                .build()
        }
        ViewCompat.setWindowInsetsAnimationCallback(this, delegate.callback)
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

        if (recyclerView == null) {
            recyclerView = child.findViewById(R.id.recycler_view)
            recyclerView?.addOnChildAttachStateChangeListener(this)
        }
    }

    override fun onChildViewAttachedToWindow(view: View) {
        val recyclerView = view.parent as RecyclerView
        val holder = recyclerView.getChildViewHolder(view) as? QueryFieldHolder
        holder ?: return
        itemView = holder.itemView
        editText = holder.itemView.findViewById(R.id.item_find_rt_find)
    }

    override fun onChildViewDetachedFromWindow(view: View) = Unit // LIER!

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
                delegate.stop(shown)
                tracker.clear()
            }
        }
        prevX = event.x
        prevY = event.y
        if (!tracking) super.dispatchTouchEvent(event)
        return true
    }

    private fun start(): Boolean {
        val recyclerView = recyclerView ?: return false
        val editText = editText ?: return false
        when {
            editText.isFocused -> Unit
            recyclerView.findFocus() != null -> return false
            else -> editText.requestFocus()
        }
        if (!imeIsVisible) {
            manager.showSoftInput(editText, 0)
        }
        delegate.reset()
        tracking = true
        controller.controlWindowInsetsAnimation(Type.ime(), -1, null, null, delegate)
        return true
    }

    private fun move(event: MotionEvent) {
        val dy = event.y - prevY
        prevY = event.y
        delegate.move(dy.roundToInt())
    }

    private fun onIme(ime: Int, end: Boolean) {
        imeIsVisible = ime > 0
        if (editText?.isFocused == true) {
            recyclerView?.translationY = -max(0, ime - bottomInset).toFloat()
        }
        if (end && !imeIsVisible) {
            recyclerView?.findFocus()?.clearFocus()
        }
    }
}