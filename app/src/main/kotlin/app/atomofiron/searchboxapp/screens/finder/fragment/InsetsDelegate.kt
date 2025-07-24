package app.atomofiron.searchboxapp.screens.finder.fragment

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsAnimationControlListenerCompat
import androidx.core.view.WindowInsetsAnimationControllerCompat

class InsetsDelegate(
    consumer: ImeListener,
) : WindowInsetsAnimationControlListenerCompat,
    ValueAnimator.AnimatorUpdateListener,
    Animator.AnimatorListener {

    private val _callback = InsetsCallback(consumer)
    val callback: WindowInsetsAnimationCompat.Callback = _callback
    private var controller: WindowInsetsAnimationControllerCompat? = null
    private var animator: ValueAnimator? = null

    override fun onReady(controller: WindowInsetsAnimationControllerCompat, types: Int) {
        this.controller = controller
    }

    override fun onFinished(controller: WindowInsetsAnimationControllerCompat) {
        this.controller = null
    }

    override fun onCancelled(controller: WindowInsetsAnimationControllerCompat?) {
        this.controller = null
    }

    fun reset() {
        animator?.cancel()
        animator = null
    }

    fun move(dy: Int) {
        val controller = controller ?: return
        val bottom = controller.currentInsets.bottom - dy
        val new = Insets.of(0, 0, 0, bottom)
        val fraction = bottom.toFloat() / controller.shownStateInsets.bottom
        controller.setInsetsAndAlpha(new, 1f, fraction)
    }

    fun stop(shown: Boolean?) {
        val controller = controller ?: return
        when {
            controller.shownStateInsets.bottom - controller.currentInsets.bottom <= 2 -> return finish(true)
            controller.currentInsets.bottom - controller.hiddenStateInsets.bottom <= 2 -> return finish(false)
        }
        val toShown = shown ?: ((controller.currentInsets.bottom.toFloat() / controller.shownStateInsets.bottom) > 0.5f)
        val target = when {
            toShown -> controller.shownStateInsets.bottom
            else -> controller.hiddenStateInsets.bottom
        }
        _callback.toShown = toShown
        animator = ValueAnimator.ofInt(controller.currentInsets.bottom, target).apply {
            addUpdateListener(this@InsetsDelegate)
            addListener(this@InsetsDelegate)
            duration = 500
            interpolator = DecelerateInterpolator(4f)
            start()
        }
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val controller = controller ?: return
        val bottom = animation.animatedValue as Int
        val new = Insets.of(0, 0, 0, bottom)
        val fraction = bottom.toFloat() / controller.shownStateInsets.bottom
        controller.setInsetsAndAlpha(new, 1f, fraction)
    }

    override fun onAnimationEnd(animation: Animator) {
        val animator = animator ?: return
        animator.removeAllUpdateListeners()
        animator.removeAllListeners()
        finish(show = animator.animatedValue != 0)
    }

    override fun onAnimationStart(animation: Animator) = Unit

    override fun onAnimationCancel(animation: Animator) = Unit

    override fun onAnimationRepeat(animation: Animator) = Unit

    private fun finish(show: Boolean) {
        _callback.toShown = show
        controller?.finish(show)
    }
}