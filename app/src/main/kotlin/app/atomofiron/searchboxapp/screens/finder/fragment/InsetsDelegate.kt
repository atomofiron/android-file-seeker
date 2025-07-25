package app.atomofiron.searchboxapp.screens.finder.fragment

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsAnimationControlListenerCompat
import androidx.core.view.WindowInsetsAnimationControllerCompat
import app.atomofiron.searchboxapp.utils.Alpha
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin

private const val Pi = PI.toFloat()
private const val ZeroPi = 0f
private const val HalfPi = Pi / 2
private const val DURATION = 256L
private val LinearInterpolator = LinearInterpolator()

class InsetsDelegate(
    listener: ImeListener,
) : WindowInsetsAnimationControlListenerCompat,
    ValueAnimator.AnimatorUpdateListener,
    Animator.AnimatorListener {

    private val _callback = InsetsCallback(listener)
    val callback: WindowInsetsAnimationCompat.Callback = _callback
    val isControlling get() = controller != null
    private var controller: WindowInsetsAnimationControllerCompat? = null
    private var animator: ValueAnimator? = null

    private var toShown = false
    private var maxInterpolated = 0 // max ime, 769
    private var minInterpolated = 0 // min ime, 0
    private var interpolated = 0 // ime
    private val interpolatedFraction get() = interpolated / maxInterpolated.toFloat()
    private val radian get() = when (toShown) { // 0..HalfPi
        true -> asin(interpolatedFraction)
        false -> acos(1 - interpolatedFraction)
    }

    override fun onReady(controller: WindowInsetsAnimationControllerCompat, types: Int) {
        this.controller = controller
        maxInterpolated = controller.shownStateInsets.bottom
        minInterpolated = controller.hiddenStateInsets.bottom
        toShown = controller.currentInsets.bottom == 0
        start(toShown)
    }

    override fun onFinished(controller: WindowInsetsAnimationControllerCompat) {
        this.controller = null
    }

    override fun onCancelled(controller: WindowInsetsAnimationControllerCompat?) {
        this.controller = null
    }

    fun reset() {
        animator?.cancel()
        reset(cancel = true)
    }

    private fun reset(cancel: Boolean) {
        if (cancel) animator?.cancel()
        animator?.removeAllUpdateListeners()
        animator?.removeAllListeners()
        animator = null
    }

    fun move(dy: Int) {
        val controller = controller ?: return
        interpolated = (interpolated - dy).coerceIn(0..maxInterpolated)
        val new = Insets.of(0, 0, 0, interpolated)
        val fraction = interpolated / maxInterpolated.toFloat()
        controller.setInsetsAndAlpha(new, 1f, fraction)
        reset()
    }

    fun start(shown: Boolean?) {
        controller ?: return
        when {
            shown == true && maxInterpolated - interpolated <= 2 -> return finish(true)
            shown == false && interpolated - minInterpolated <= 2 -> return finish(false)
        }
        toShown = shown ?: (interpolated > maxInterpolated / 2)
        val target = if (toShown) HalfPi else ZeroPi
        val radian = radian
        animator = ValueAnimator.ofFloat(radian, target).apply {
            addUpdateListener(this@InsetsDelegate)
            addListener(this@InsetsDelegate)
            duration = (DURATION * abs(target - radian) / HalfPi).toLong()
            interpolator = LinearInterpolator
            start()
        }
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val controller = controller ?: return
        var value = animation.animatedValue as Float
        value = when (toShown) {
            true -> sin(value)
            false -> 1 - cos(value)
        }
        val bottom = value * maxInterpolated
        interpolated = bottom.toInt()
        val fraction = bottom / maxInterpolated
        val new = Insets.of(0, 0, 0, interpolated)
        controller.setInsetsAndAlpha(new, Alpha.VISIBLE, fraction)
    }

    override fun onAnimationEnd(animation: Animator) {
        reset(cancel = false)
        finish()
    }

    override fun onAnimationCancel(animation: Animator) = reset(cancel = false)

    override fun onAnimationStart(animation: Animator) = Unit

    override fun onAnimationRepeat(animation: Animator) = Unit

    private fun finish(show: Boolean = interpolated > 0) {
        toShown = show
        controller?.finish(show)
    }
}