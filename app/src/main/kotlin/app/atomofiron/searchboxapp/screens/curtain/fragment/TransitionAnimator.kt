package app.atomofiron.searchboxapp.screens.curtain.fragment

import android.animation.ValueAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import app.atomofiron.searchboxapp.databinding.FragmentCurtainBinding
import app.atomofiron.searchboxapp.utils.Const
import kotlin.math.abs

class TransitionAnimator(
    private val binding: FragmentCurtainBinding,
    private val transitionCallback: () -> Unit,
) : View.OnLayoutChangeListener, ValueAnimator.AnimatorUpdateListener {
    companion object {
        private const val START_VALUE = 0f
        private const val END_VALUE = 1f
    }
    var transitionIsRunning = false
        private set

    private var forward = true
    private var from = 0
    private var to = 0
    private lateinit var leftView: View
    private lateinit var rightView: View

    private val animator = ValueAnimator.ofFloat(START_VALUE, END_VALUE)

    init {
        animator.interpolator = DecelerateInterpolator()
        animator.duration = Const.COMMON_DURATION
    }

    fun startTransition(forward: Boolean) {
        this.forward = forward
        transitionIsRunning = true
        binding.curtainSheet.addOnLayoutChangeListener(this)
    }

    override fun onLayoutChange(view: View, l: Int, t: Int, r: Int, b: Int, oL: Int, oT: Int, oR: Int, oB: Int) {
        view.removeOnLayoutChangeListener(this)
        transitionIsRunning = false
        leftView = binding.curtainSheet.getChildAt(0) ?: return
        rightView = binding.curtainSheet.getChildAt(1) ?: return
        transitionIsRunning = true
        from = if (forward) leftView.height else rightView.height
        to = if (forward) rightView.height else leftView.height
        animator.removeUpdateListener(this)
        animator.addUpdateListener(this)
        animator.start()
    }

    override fun onAnimationUpdate(animator: ValueAnimator) {
        val value = animator.animatedValue as Float
        val dif = abs(from - to).toFloat()
        val sheetTop = when {
            from > to -> dif * value
            else -> dif - dif * value
        }
        binding.curtainSheet.run {
            val height = height
            top = sheetTop.toInt()
            bottom = top + height
        }
        val width = binding.curtainSheet.width
        val center = when {
            forward -> (width - width * value).toInt()
            else -> (width * value).toInt()
        }
        leftView.left = center - width
        leftView.right = center
        rightView.left = center
        rightView.right = center + width
        transitionCallback()
        if (value == END_VALUE) {
            transitionIsRunning = false
            this.animator.removeUpdateListener(this)
            when {
                forward -> binding.curtainSheet.removeView(leftView)
                else -> binding.curtainSheet.removeView(rightView)
            }
        }
    }
}