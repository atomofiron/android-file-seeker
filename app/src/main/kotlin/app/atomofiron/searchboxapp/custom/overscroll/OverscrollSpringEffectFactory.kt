package app.atomofiron.searchboxapp.custom.overscroll

import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.EdgeEffect
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.utils.setFloatValues
import kotlin.math.max
import kotlin.math.min

fun RecyclerView.setupSpringOverscroll() {
    val factory = OverscrollSpringEffectFactory()
    addOnChildAttachStateChangeListener(factory.attachChildListener)
    addOnItemTouchListener(factory.touchListener)
    edgeEffectFactory = factory
}

private class OverscrollSpringEffectFactory : RecyclerView.EdgeEffectFactory() {
    private enum class Direction(val sign: Int) {
        Up(-1),
        Down(1),
    }

    private var touchY = 0f

    override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
        return when (direction) {
            DIRECTION_TOP -> SpringEffect(view, Direction.Down)
            DIRECTION_BOTTOM -> SpringEffect(view, Direction.Up)
            else -> super.createEdgeEffect(view, direction)
        }
    }

    val touchListener = object : RecyclerView.SimpleOnItemTouchListener() {
        override fun onInterceptTouchEvent(view: RecyclerView, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> touchY = event.y
                MotionEvent.ACTION_MOVE -> touchY = event.y
            }
            return false
        }
    }

    val attachChildListener = object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewAttachedToWindow(view: View) = Unit
        override fun onChildViewDetachedFromWindow(view: View) {
            view.translationY = 0f
        }
    }

    private inner class SpringEffect(
        private val view: RecyclerView,
        private val direction: Direction,
    ) : EdgeEffect(view.context), ValueAnimator.AnimatorUpdateListener {

        private val halfDuration = 512L * 2
        private var maxVelocity = 21000

        private var distance = 0f
        private val zeroDistance = 0f
        private val minDistance = .01f

        private val animator = ValueAnimator.ofFloat(0f)
        private var released = true

        init {
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener(this)
        }

        override fun onPull(deltaDistance: Float) {
            onPullDistance(deltaDistance, .5f)
        }

        override fun onPull(deltaDistance: Float, displacement: Float) {
            onPullDistance(deltaDistance, displacement)
        }

        override fun onPullDistance(deltaDistance: Float, displacement: Float): Float {
            released = false
            animator.cancel()

            var delta = deltaDistance
            val distance = distance + delta
            if (distance < zeroDistance) {
                delta -= distance
            }
            applyEffect(delta)
            return delta
        }

        override fun getDistance(): Float = distance

        override fun isFinished(): Boolean = released && !animator.isStarted

        override fun onRelease() {
            released = true
            when {
                animator.isStarted -> Unit
                distance > minDistance -> onAbsorb(velocity = 0)
                else -> reset()
            }
        }

        override fun onAbsorb(velocity: Int) {
            maxVelocity = max(maxVelocity, velocity)
            val animDistance: Float
            animator.interpolator = when {
                distance < minDistance && velocity <= 0 -> return reset()
                distance >= minDistance -> {
                    animator.setFloatValues(distance, zeroDistance)
                    animDistance = distance
                    Interpolators.SpringOut
                }
                else -> {
                    touchY = when (direction) {
                        Direction.Down -> view.height.toFloat()
                        Direction.Up -> 0f
                    }
                    val overDistance = velocity / maxVelocity.toFloat()
                    animator.setFloatValues(distance, 1 to overDistance, 9 to zeroDistance)
                    animDistance = overDistance * 2
                    CompositeInterpolator { of(SinOut to .1f, final = SpringOut) }
                }
            }
            animator.duration = (halfDuration * animDistance).toLong()
                .let { min(it, halfDuration) }
                .let { it + halfDuration }
            animator.start()
        }

        override fun finish() = Unit

        override fun onAnimationUpdate(animation: ValueAnimator) {
            val new = animation.animatedValue as Float
            val delta = new - distance
            applyEffect(delta)
        }

        private fun applyEffect(delta: Float) {
            distance += delta
            val offset = distance * view.height * direction.sign / 3
            view.applyEffect(offset, direction)
            view.parent?.requestDisallowInterceptTouchEvent(true)
        }

        private fun reset() {
            for (child in view.children) {
                child.translationY = 0f
            }
        }

        private fun RecyclerView.applyEffect(offset: Float, direction: Direction) {
            val touchY = when (direction) {
                Direction.Up -> height - touchY
                Direction.Down -> touchY
            }
            for (child in children) {
                val childEdge = when (direction) {
                    Direction.Up -> height - child.y
                    Direction.Down -> child.height + child.y
                }
                when {
                    touchY > childEdge -> child.translationY = childEdge / touchY * offset
                    else -> child.translationY = offset
                }
            }
            invalidate()
        }
    }
}
