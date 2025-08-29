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
    var isControllableNow = Android.R
        private set
    var visible = false
        private set

    override fun onApplyWindowInsets(windowInsets: ExtendedWindowInsets) {
        val current = windowInsets[ExtType.ime].bottom
        if (!isControllableNow) {
            if (current > 0) {
                max = current
            }
            if (max > 0 && visible != (current > 0)) {
                visible = current > 0
                listeners.forEach { it.onImeStart(max) }
                listeners.forEach { it.onImeMove(current) }
                listeners.forEach { it.onImeEnd(visible = current > 0) }
            }
        } else if (!visible && current > 0 && !isPrepareCalled) {
            // it just got broken
            isControllableNow = false
        }
    }

    override fun onPrepare(animation: WindowInsetsAnimationCompat) {
        isPrepareCalled = true
    }

    override fun onStart(
        animation: WindowInsetsAnimationCompat,
        bounds: BoundsCompat,
    ): BoundsCompat {
        isPrepareCalled = false
        isControllableNow = true
        val anim = animation.takeIf { it.typeMask == Type.ime() }
        anim ?: return bounds
        max = bounds.upperBound.bottom
        listeners.forEach { it.onImeStart(bounds.upperBound.bottom) }
        return bounds
    }

    override fun onProgress(
        insets: WindowInsetsCompat,
        runningAnimations: List<WindowInsetsAnimationCompat>,
    ): WindowInsetsCompat {
        val anim = runningAnimations.find { it.typeMask == Type.ime() }
        anim ?: return insets
        val ime = insets.getInsets(Type.ime()).bottom
        visible = ime > 0
        listeners.forEach { it.onImeMove(ime) }
        return insets
    }

    override fun onEnd(animation: WindowInsetsAnimationCompat) {
        // when app was minimized
        listeners.forEach { it.onImeEnd(visible) }
    }
}
