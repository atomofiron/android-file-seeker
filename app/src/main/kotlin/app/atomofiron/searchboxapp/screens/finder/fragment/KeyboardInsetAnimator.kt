package app.atomofiron.searchboxapp.screens.finder.fragment

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import androidx.core.graphics.Insets
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

class InsetsAnimator : WindowInsetsAnimationControlListenerCompat,
    ValueAnimator.AnimatorUpdateListener,
    Animator.AnimatorListener,
    KeyboardInsetListener {

    val isControlling get() = controller != null
    private var controller: WindowInsetsAnimationControllerCompat? = null
    private var animator: ValueAnimator? = null

    private var toVisible = false
    private var maxInterpolated = 0 // max ime, e.g. 769
    private var minInterpolated = 0 // min ime, 0?
    private var interpolated = 0 // ime e.g. 0..769
    private val interpolatedFraction get() = /* 0..1 */ interpolated / maxInterpolated.toFloat()
    private val radian get() = /* 0..HalfPi */ when (toVisible) {
        true -> asin(interpolatedFraction)
        false -> acos(1 - interpolatedFraction)
    }

    override fun onReady(controller: WindowInsetsAnimationControllerCompat, types: Int) {
        this.controller = controller
        maxInterpolated = controller.shownStateInsets.bottom
        minInterpolated = controller.hiddenStateInsets.bottom
        toVisible = controller.currentInsets.bottom == 0
        start(toVisible)
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
        val new = (interpolated - dy).coerceIn(0..maxInterpolated)
        val insets = Insets.of(0, 0, 0, new)
        val fraction = new / maxInterpolated.toFloat()
        controller.setInsetsAndAlpha(insets, 1f, fraction)
        reset()
    }

    fun start(shown: Boolean?) {
        controller ?: return
        when {
            shown == true && maxInterpolated - interpolated <= 2 -> return finish(true)
            shown == false && interpolated - minInterpolated <= 2 -> return finish(false)
        }
        toVisible = shown ?: (interpolated > maxInterpolated / 2)
        val target = if (toVisible) HalfPi else ZeroPi
        val radian = radian
        animator = ValueAnimator.ofFloat(radian, target).apply {
            addUpdateListener(this@InsetsAnimator)
            addListener(this@InsetsAnimator)
            duration = (DURATION * abs(target - radian) / HalfPi).toLong()
            interpolator = LinearInterpolator
            start()
        }
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        val controller = controller ?: return
        var value = animation.animatedValue as Float
        value = when (toVisible) {
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
        toVisible = show
        controller?.finish(show)
    }

    override fun onImeStart(max: Int) { // this one is called before the onReady
        maxInterpolated = max
    }

    override fun onImeMove(current: Int) {
        interpolated = current
    }

    override fun onImeEnd(visible: Boolean) {
        toVisible = visible
        interpolated = if (visible) maxInterpolated else minInterpolated
    }
}