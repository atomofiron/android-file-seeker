package app.atomofiron.searchboxapp.screens.finder.fragment

import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsAnimationCompat.BoundsCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type

class InsetsCallback(
    private val listener: ImeListener,
) : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {

    override fun onStart(animation: WindowInsetsAnimationCompat, bounds: BoundsCompat): BoundsCompat {
        val anim = animation.takeIf { it.typeMask == Type.ime() }
        anim ?: return bounds
        listener.onImeStart(bounds.upperBound.bottom)
        return bounds
    }

    override fun onProgress(
        insets: WindowInsetsCompat,
        runningAnimations: List<WindowInsetsAnimationCompat>,
    ): WindowInsetsCompat {
        val anim = runningAnimations.find { it.typeMask == Type.ime() }
        anim ?: return insets
        val ime = insets.getInsets(Type.ime())
        listener.onImeMove(ime.bottom)
        return insets
    }

    override fun onEnd(animation: WindowInsetsAnimationCompat) {
        // when app was minimized
        listener.onImeEnd()
    }
}