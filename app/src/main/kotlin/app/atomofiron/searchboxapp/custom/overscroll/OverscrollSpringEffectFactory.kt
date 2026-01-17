package app.atomofiron.searchboxapp.custom.overscroll

import android.animation.ValueAnimator
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
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

    private var startY = 0f
    private var pullY = 0f
    private var ignorePulling = false
    private var resetOnPull = false

    override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
        return when (direction) {
            DIRECTION_TOP -> SpringEffect(view, Direction.Down)
            DIRECTION_BOTTOM -> SpringEffect(view, Direction.Up)
            else -> super.createEdgeEffect(view, direction)
        }
    }

    val touchListener = object : RecyclerView.SimpleOnItemTouchListener() {

        override fun onInterceptTouchEvent(view: RecyclerView, event: MotionEvent): Boolean {
            if (event.pointerCount > 1) {
                ignorePulling = true
                resetOnPull = true
            } else when (event.action) {
                ACTION_DOWN -> {
                    resetOnPull = true
                    startY = event.y
                }
                ACTION_MOVE -> pullY = event.y
                ACTION_CANCEL,
                ACTION_UP -> ignorePulling = false
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

        private val halfDuration = 512L
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
            onPullDistance(deltaDistance)
        }

        override fun onPull(deltaDistance: Float, displacement: Float) {
            onPullDistance(deltaDistance)
        }

        override fun onPullDistance(deltaDistance: Float, displacement: Float): Float {
            return onPullDistance(deltaDistance)
        }

        private fun onPullDistance(deltaDistance: Float): Float {
            if (resetOnPull || animator.isStarted) {
                resetOnPull = false
                reset()
            }
            if (ignorePulling) {
                return 0f
            }
            released = false

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
                    val height = view.height.toFloat()
                    startY = when (direction) {
                        Direction.Down -> height
                        Direction.Up -> 0f
                    }
                    pullY = when (direction) {
                        Direction.Down -> height * 2
                        Direction.Up -> -height
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
            applyEffect(delta = new - distance)
        }

        private fun applyEffect(delta: Float) {
            distance += delta
            val offset = distance * view.height / 3
            view.applyEffect(offset, direction)
            view.parent?.requestDisallowInterceptTouchEvent(true)
        }

        private fun reset() {
            animator.cancel()
            if (distance != zeroDistance) {
                distance = zeroDistance
                for (child in view.children) {
                    child.translationY = 0f
                }
            }
        }

        private fun RecyclerView.applyEffect(offset: Float, direction: Direction) {
            val childStart = when (direction) {
                Direction.Down -> paddingTop
                Direction.Up -> paddingBottom
            }
            val startY = when (direction) {
                Direction.Down -> startY
                Direction.Up -> height - startY
            }
            val touchY = when (direction) {
                Direction.Down -> pullY
                Direction.Up -> height - pullY
            }
            val swipe = touchY - startY
            for (child in children) {
                val childEdge = when (direction) {
                    Direction.Down -> child.bottom
                    Direction.Up -> height - child.top
                }
                val slowOffset = (childEdge - childStart) / (touchY - childStart) * offset
                child.translationY = direction.sign * when {
                    childEdge <= startY -> slowOffset // items are above/below the finger
                    childEdge + offset >= touchY -> offset // items are below/above the finger (in the current iteration)
                    else -> { // at first they were below/above, later they became above/below
                        val threshold = startY + swipe * (childEdge - startY) / (swipe - offset)
                        val fastPart = offset * (threshold - startY) / swipe
                        val slowPart = slowOffset * (touchY - threshold) / swipe
                        fastPart + slowPart
                    }
                }
            }
            invalidate()
        }
    }
}

/*
finger's path:    |--------X------->
item edge's path:       |--X->
X (threshold) - the moment after which the item should move slower
*/
