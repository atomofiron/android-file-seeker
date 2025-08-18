package app.atomofiron.searchboxapp.screens.finder.fragment.keyboard

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsAnimationControlListenerCompat
import androidx.core.view.WindowInsetsAnimationControllerCompat
import app.atomofiron.searchboxapp.utils.Alpha
import kotlin.math.abs

const val KEYBOARD_DURATION = 256L

class InsetsAnimator(
    private val anyFocused: () -> Boolean,
) : WindowInsetsAnimationControlListenerCompat {

    val keyboardListener: KeyboardInsetListener = KeyboardListener()

    private var controller: WindowInsetsAnimationControllerCompat? = null
    private var animator: ValueAnimator? = null

    private var toVisible = false
    private var keyboardNow = 0 // ime e.g. 0..769
    private var keyboardMin = 0 // min ime, 0?
    private var keyboardMax = 0 // max ime, e.g. 769

    override fun onReady(controller: WindowInsetsAnimationControllerCompat, types: Int) {
        this.controller = controller
        keyboardMax = controller.shownStateInsets.bottom
        keyboardMin = controller.hiddenStateInsets.bottom
        toVisible = anyFocused() // this is valid because the onReady is called quite late
    }

    override fun onFinished(controller: WindowInsetsAnimationControllerCompat) {
        this.controller = null
    }

    override fun onCancelled(controller: WindowInsetsAnimationControllerCompat?) {
        this.controller = null
    }

    fun resetAnimation() = resetAnimation(cancel = true)

    private fun resetAnimation(cancel: Boolean) {
        if (cancel) animator?.cancel()
        animator?.removeAllUpdateListeners()
        animator?.removeAllListeners()
        animator = null
    }

    fun move(dy: Int) {
        val controller = controller ?: return
        resetAnimation()
        val new = (keyboardNow - dy).coerceIn(0..keyboardMax)
        val insets = Insets.of(0, 0, 0, new)
        val fraction = new / keyboardMax.toFloat()
        controller.setInsetsAndAlpha(insets, 1f, fraction)
    }

    fun start(shown: Boolean?) {
        controller ?: return
        when {
            shown == true && keyboardMax - keyboardNow <= 2 -> return finish(true)
            shown == false && keyboardNow - keyboardMin <= 2 -> return finish(false)
        }
        toVisible = shown ?: (keyboardNow > keyboardMax / 2)
        val from = keyboardNow
        val to = if (toVisible) keyboardMax else keyboardMin
        if (from != to) {
            resetAnimation()
            animator = ValueAnimator.ofInt(from, to).apply {
                addUpdateListener(AnimatorUpdateListener())
                addListener(AnimationListener())
                duration = (KEYBOARD_DURATION * abs(to - from) / keyboardMax.toFloat()).toLong()
                interpolator = DecelerateInterpolator()
                start()
            }
        }
    }

    fun finish(show: Boolean = keyboardNow > 0) {
        toVisible = show
        controller?.finish(show)
    }

    private inner class AnimatorUpdateListener : ValueAnimator.AnimatorUpdateListener {

        override fun onAnimationUpdate(animation: ValueAnimator) {
            val controller = controller ?: return
            keyboardNow = animation.animatedValue as Int
            val new = Insets.of(0, 0, 0, keyboardNow)
            controller.setInsetsAndAlpha(new, Alpha.VISIBLE, 1f)
        }
    }

    private inner class AnimationListener : Animator.AnimatorListener {

        override fun onAnimationEnd(animation: Animator) {
            resetAnimation(cancel = false)
            finish()
        }

        override fun onAnimationCancel(animation: Animator) = resetAnimation(cancel = false)

        override fun onAnimationStart(animation: Animator) = Unit

        override fun onAnimationRepeat(animation: Animator) = Unit
    }

    private inner class KeyboardListener : KeyboardInsetListener {

        override fun onImeStart(max: Int) { // this one is called before the onReady
            keyboardMax = max
        }

        override fun onImeMove(current: Int) {
            keyboardNow = current
        }

        override fun onImeEnd(visible: Boolean) {
            toVisible = visible
            keyboardNow = if (visible) keyboardMax else keyboardMin
            resetAnimation()
        }
    }
}