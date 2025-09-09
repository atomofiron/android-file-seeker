package app.atomofiron.searchboxapp.screens.curtain.fragment

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.Window
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.atomofiron.searchboxapp.screens.finder.fragment.keyboard.KeyboardInsetCallback
import app.atomofiron.searchboxapp.screens.finder.fragment.keyboard.KeyboardInsetListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import lib.atomofiron.insets.ExtendedWindowInsets
import kotlin.math.max

class BottomSheetKeyboardBehavior<V : View>(
    context: Context,
    attrs: AttributeSet?,
) : BottomSheetBehavior<V>(context, attrs), KeyboardInsetListener {

    private val keyboardCallback = KeyboardInsetCallback(this)
    private lateinit var controller: WindowInsetsControllerCompat
    private lateinit var parent: CoordinatorLayout
    private lateinit var child: V

    private var isControlling = false
    private var bottomPadding = 0

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        this.parent = parent
        this.child = child
        ViewCompat.setWindowInsetsAnimationCallback(parent, keyboardCallback)
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    fun setWindow(window: Window) {
        controller = WindowCompat.getInsetsController(window, window.decorView)
    }

    fun onApplyWindowInsets(windowInsets: ExtendedWindowInsets, bottomPadding: Int) {
        this.bottomPadding = bottomPadding
        keyboardCallback.onApplyWindowInsets(windowInsets)
    }

    override fun onImeMove(current: Int) {
        parent.translationY = -max(0, current - bottomPadding).toFloat()
    }

    override fun onImeEnd(visible: Boolean) {
        isControlling = false
    }
}