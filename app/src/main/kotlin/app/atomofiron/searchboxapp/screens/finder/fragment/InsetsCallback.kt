package app.atomofiron.searchboxapp.screens.finder.fragment

import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsAnimationCompat.BoundsCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsCompat.Type

class InsetsCallback(
    private val listener: ImeListener,
) : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_STOP) {

    private var imeHeight = 0
    var toShown: Boolean? = null

    override fun onStart(animation: WindowInsetsAnimationCompat, bounds: BoundsCompat): BoundsCompat {
        imeHeight = bounds.upperBound.bottom
        toShown = null
        return bounds
    }

    override fun onProgress(
        insets: WindowInsetsCompat,
        runningAnimations: List<WindowInsetsAnimationCompat>,
    ): WindowInsetsCompat {
        val anim = runningAnimations.find { it.typeMask == Type.ime() }
        anim ?: return insets
        val ime = insets.getInsets(Type.ime())
        if (toShown == null) {
            toShown = ime.bottom < imeHeight / 2
        }
        listener(ime.bottom, end = false)
        return insets
    }

    override fun onEnd(animation: WindowInsetsAnimationCompat) {
        // when app was minimized
        when (toShown) {
            true -> listener(imeHeight, end = true)
            false -> listener(0, end = true)
            null -> Unit
        }
    }
}