package app.atomofiron.searchboxapp.screens.finder.fragment.keyboard

import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsAnimationCompat.BoundsCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type
import app.atomofiron.common.util.Android
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.ExtendedWindowInsets
import lib.atomofiron.insets.InsetsListener

class KeyboardInsetCallback(
    private vararg val listeners: KeyboardInsetListener,
) : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP), InsetsListener {

    private var max = 0
    private var isPrepareCalled = false
    var isControllable = Android.R
        private set
    var visible = false
        private set

    override fun onApplyWindowInsets(windowInsets: ExtendedWindowInsets) {
        val current = windowInsets[ExtType.ime].bottom
        if (isControllable && current > 0 && !visible && !isPrepareCalled) {
            // it just got broken
            isControllable = false
            listeners.forEach { it.onImeBroke(visible) }
        }
        if (!isControllable || current > 0 && max > 0 && current != max) {
            notifyListeners(current)
        }
    }

    private fun notifyListeners(current: Int) {
        if (current > 0) {
            max = current
        }
        if (max > 0) {
            visible = current > 0
            listeners.forEach { it.onImeStart(max) }
            listeners.forEach { it.onImeMove(current) }
            listeners.forEach { it.onImeEnd(visible = current > 0) }
        }
    }

    override fun onPrepare(animation: WindowInsetsAnimationCompat) {
        if (animation.typeMask == Type.ime()) {
            isPrepareCalled = true
        }
    }

    override fun onStart(
        animation: WindowInsetsAnimationCompat,
        bounds: BoundsCompat,
    ): BoundsCompat {
        if (animation.typeMask == Type.ime()) {
            isPrepareCalled = false
            isControllable = true
            max = bounds.upperBound.bottom
            listeners.forEach { it.onImeStart(max) }
        }
        return bounds
    }

    override fun onProgress(
        insets: WindowInsetsCompat,
        runningAnimations: List<WindowInsetsAnimationCompat>,
    ): WindowInsetsCompat {
        if (runningAnimations.any { it.typeMask == Type.ime() }) {
            val ime = insets.getInsets(Type.ime()).bottom
            visible = ime > 0
            listeners.forEach { it.onImeMove(ime) }
        }
        return insets
    }

    override fun onEnd(animation: WindowInsetsAnimationCompat) {
        // on app was minimized
        if (animation.typeMask == Type.ime()) {
            listeners.forEach { it.onImeEnd(visible) }
        }
    }
}
