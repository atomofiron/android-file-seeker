package app.atomofiron.searchboxapp.custom.view.dock.popup

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.graphics.drawable.LayerDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewOutlineProvider
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.graphics.withClip
import androidx.core.view.updateLayoutParams
import app.atomofiron.common.util.coerceInRange
import app.atomofiron.common.util.extension.corner
import app.atomofiron.common.util.extension.nearby
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemDockBinding
import app.atomofiron.searchboxapp.custom.drawable.PathDrawable
import app.atomofiron.searchboxapp.custom.view.dock.DockMode
import app.atomofiron.searchboxapp.custom.view.dock.DockViewImpl
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemConfig
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemHolder
import app.atomofiron.searchboxapp.model.Layout.Ground
import app.atomofiron.searchboxapp.utils.toIntAlpha
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val DURATION = 512L
private const val DELAY = 256L
private const val COLLAPSED = 0f
private const val EXPANDED = 1f
private const val CROSS_ROTATION = 45

@SuppressLint("ViewConstructor")
class DockItemChildrenView(
    context: Context,
    item: DockItem,
    private val config: DockItemConfig,
    private val selectListener: (DockItem) -> Unit,
) : FrameLayout(context), ValueAnimator.AnimatorUpdateListener {

    private val ghost = LayoutInflater.from(context)
        .let { ItemDockBinding.inflate(it, this, true) }
        .also { DockItemHolder(it) { collapse() }.bind(item.withOutChildren(), config) }
    private val childrenView = DockViewImpl(context, item.children, config, mode = DockMode.Popup(config.popup, item.children.columns))
    private val backgroundPath = Path()
    private var clipPath = Path()
    private var combinedPath = Path()
    private val corner = resources.getDimension(R.dimen.dock_overlay_corner)
    private val offset = resources.getDimension(R.dimen.dock_item_half_margin).roundToInt().toFloat()
    private val animator = ValueAnimator.ofFloat(COLLAPSED)
    private var currentValue = COLLAPSED
    private var targetValue = COLLAPSED
    private val popupElevation = resources.getDimension(R.dimen.overlay_elevation)
    private val ghostDrawable = ghost.icon.drawable!!
    private val crossDrawable = ContextCompat.getDrawable(context, R.drawable.ic_plus)!!

    init {
        if (item.children.isEmpty()) {
            throw IllegalArgumentException("DockItem hasn't children: $item")
        }
        setWillNotDraw(false)
        ghost.root.setOnClickListener { collapse() }
        ghost.root.updateLayoutParams {
            width = config.width
            height = config.height
        }
        crossDrawable.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(ghost.icon.imageTintList!!.defaultColor, BlendModeCompat.SRC_IN)
        ghost.icon.foreground = crossDrawable // LayerDrawable make alpha always 255, overriding of LayerDrawable doesn't help
        ghost.button.clipChildren = false
        childrenView.outlineProvider = ChildrenOutlineProvider(corner, config.popup.ground, config.popup.style.strokeWidth.roundToInt())
        childrenView.clipToOutline = true
        childrenView.elevation = 0f
        childrenView.setListener(::onSelect)
        addView(childrenView)
        childrenView.updateLayoutParams<LayoutParams> {
            width = WRAP_CONTENT
            height = WRAP_CONTENT
            gravity = when (config.popup.ground) {
                Ground.Bottom -> Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                Ground.Left -> Gravity.CENTER_VERTICAL or Gravity.LEFT
                Ground.Right -> Gravity.CENTER_VERTICAL or Gravity.RIGHT
            }
        }
        animator.addUpdateListener(this)
        animator.interpolator = DecelerateInterpolator()
        val style = config.popup.style
        background = LayerDrawable(arrayOf(
            PathDrawable.stroke(combinedPath, config.popup.stroke, style.strokeWidth * 2),
            PathDrawable.fill(combinedPath, style.fill),
        ))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        childrenView.place()
        updateBackgroundPath()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return super.dispatchTouchEvent(event).also {
            if (!it) remove()
        }
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        currentValue = animation.animatedValue as Float
        if (currentValue == COLLAPSED && targetValue == COLLAPSED) {
            return remove()
        }
        val centerX = config.width / 2f
        val centerY = config.height / 2f
        val itemRadius = config.run { sqrt(width * width + height * height.toFloat()) / 2 }
        val popupRadius = childrenView.run { sqrt(width * width + height * height.toFloat()) }
        val maxRadius = itemRadius + popupRadius
        val radius = maxRadius * currentValue
        combinedPath.set(backgroundPath)
        if (currentValue > COLLAPSED && currentValue < EXPANDED) {
            clipPath.reset()
            clipPath.addCircle(centerX, centerY, radius, Path.Direction.CW)
            combinedPath.op(backgroundPath, clipPath, Path.Op.INTERSECT)
        }
        val dotOffset = (itemRadius + radius).coerceAtLeast(maxRadius.dec())
        var (dotX, dotY) = when (config.popup.ground) {
            Ground.Left -> -dotOffset to 0f
            Ground.Right -> dotOffset to 0f
            Ground.Bottom -> 0f to dotOffset
        }
        dotX += centerX
        dotY += centerY
        // fixes shadow offset
        combinedPath.addRect(dotX, dotY, dotX.dec(), dotY.dec(), Path.Direction.CW)
        invalidate()
        invalidateOutline()

        val half = ((radius - itemRadius) / (popupRadius / 2)).coerceInRange(0f, 1f)
        val iconCenter = ghost.icon.run { top + bottom } / 2
        val buttonCenter = ghost.button.height / 2
        val offset = (buttonCenter - iconCenter) / 2 * half
        val ghostAlpha = 1f - half
        ghostDrawable.alpha = ghostAlpha.toIntAlpha()
        crossDrawable.alpha = half.toIntAlpha()
        ghost.icon.rotation = CROSS_ROTATION * half
        ghost.icon.translationY = offset
        ghost.label.translationY = offset
        ghost.label.alpha = ghostAlpha
        elevation = popupElevation * half
    }

    override fun draw(canvas: Canvas) {
        if (currentValue == EXPANDED) {
            super.draw(canvas)
        } else if (currentValue > COLLAPSED) {
            // can't clip children by the clipToOutline because of outline.setPath() doesn't work on android 12-13
            canvas.withClip(clipPath) {
                super.draw(canvas)
            }
        }
    }

    fun expand() = animTo(EXPANDED)

    fun collapse() = collapse(withDelay = false)

    private fun collapse(withDelay: Boolean) = when {
        targetValue == COLLAPSED && !withDelay -> remove()
        else -> animTo(COLLAPSED, withDelay)
    }

    private fun remove() {
        animator.cancel()
        (parent as DockPopupLayout).clear()
    }

    private fun animTo(value: Float, withDelay: Boolean = false) {
        if (value == targetValue) {
            return
        }
        targetValue = value
        animator.cancel()
        animator.startDelay = if (withDelay) DELAY else 0L
        animator.setFloatValues(currentValue, targetValue)
        animator.duration = (abs(targetValue - currentValue) * DURATION).toLong()
        animator.start()
    }

    private fun onSelect(item: DockItem) {
        collapse(withDelay = true)
        selectListener(item)
    }

    private fun DockViewImpl.place() {
        val rect = config.popup.rect
        val ground = config.popup.ground
        translationX = when (ground) {
            Ground.Bottom -> when {
                left < rect.left -> rect.left - left
                right > rect.right -> rect.right - right
                else -> 0
            }.toFloat()
            Ground.Left -> offset + config.width
            Ground.Right -> -(offset + config.width)
        }
        translationY = when (ground) {
            Ground.Bottom -> -offset - config.height
            Ground.Left, Ground.Right -> when {
                top < rect.top -> rect.top - top
                top > rect.top -> rect.top - top
                else -> 0
            }.toFloat()
        }
    }

    private fun updateBackgroundPath() = backgroundPath.run {
        val ground = config.popup.ground
        val inset = config.popup.style.strokeWidth
        var closeLeft = ghost.root.x - offset
        var closeTop = ghost.root.y - offset
        var closeRight = closeLeft + config.width + offset * 2
        var closeBottom = closeTop + config.height + offset * 2
        var popupLeft = childrenView.x
        var popupTop = childrenView.y
        var popupRight = popupLeft + childrenView.width
        var popupBottom = popupTop + childrenView.height
        val distance = corner * 2
        val topLeft = !nearby(distance, popupLeft, popupTop, closeRight, closeTop)
        val topRight = !nearby(distance, popupRight, popupTop, closeLeft, closeTop)
        val bottomRight = !nearby(distance, popupRight, popupBottom, closeRight, closeTop, closeLeft, closeBottom)
        val bottomLeft = !nearby(distance, popupLeft, popupBottom, closeLeft, closeTop, closeRight, closeBottom)
        closeBottom -= inset
        popupTop += inset
        when (ground) {
            Ground.Bottom -> {
                closeLeft += inset
                closeRight -= inset
                popupLeft += inset
                popupRight -= inset
            }
            Ground.Right -> {
                closeTop += inset
                closeRight -= inset
                popupLeft += inset
                popupBottom -= inset
            }
            Ground.Left -> {
                closeLeft += inset
                closeTop += inset
                popupRight -= inset
                popupBottom -= inset
            }
        }
        reset()
        when (ground) {
            Ground.Bottom -> {
                if (bottomLeft) corner(closeLeft, closeTop, left = false, top = true, clockWise = true, radius = corner) else moveTo(closeLeft, closeTop)
                corner(closeLeft, closeBottom, left = true, top = false, clockWise = false, radius = corner)
                corner(closeRight, closeBottom, left = false, top = false, clockWise = false, radius = corner)
                if (bottomRight) corner(closeRight, closeTop, left = true, top = true, clockWise = true, radius = corner) else lineTo(closeRight, closeTop)
                if (bottomRight) corner(popupRight, popupBottom, left = false, top = false, clockWise = false, radius = corner) else lineTo(popupRight, popupBottom)
                if (topRight) corner(popupRight, popupTop, left = false, top = true, clockWise = false, radius = corner) else lineTo(popupRight, popupTop)
                if (topLeft) corner(popupLeft, popupTop, left = true, top = true, clockWise = false, radius = corner) else lineTo(popupLeft, popupTop)
                if (bottomLeft) corner(popupLeft, popupBottom, left = true, top = false, clockWise = false, radius = corner) else lineTo(popupLeft, popupBottom)
            }
            Ground.Right -> {
                if (bottomRight) corner(closeLeft, closeBottom, left = true, top = true, clockWise = true, radius = corner) else moveTo(closeLeft, closeBottom)
                corner(closeRight, closeBottom, left = false, top = false, clockWise = false, radius = corner)
                corner(closeRight, closeTop, left = false, top = true, clockWise = false, radius = corner)
                if (topRight) corner(closeLeft, closeTop, left = true, top = false, clockWise = true, radius = corner) else lineTo(closeLeft, closeTop)
                if (topRight) corner(popupRight, popupTop, left = false, top = true, clockWise = false, radius = corner) else lineTo(popupRight, popupTop)
                if (topLeft) corner(popupLeft, popupTop, left = true, top = true, clockWise = false, radius = corner) else lineTo(popupLeft, popupTop)
                if (bottomLeft) corner(popupLeft, popupBottom, left = true, top = false, clockWise = false, radius = corner) else lineTo(popupLeft, popupBottom)
                if (bottomRight) corner(popupRight, popupBottom, left = false, top = false, clockWise = false, radius = corner) else lineTo(popupRight, popupBottom)
            }
            Ground.Left -> {
                if (topLeft) corner(closeRight, closeTop, left = false, top = false, clockWise = true, radius = corner) else moveTo(closeRight, closeTop)
                corner(closeLeft, closeTop, left = true, top = true, clockWise = false, radius = corner)
                corner(closeLeft, closeBottom, left = true, top = false, clockWise = false, radius = corner)
                if (bottomLeft) corner(closeRight, closeBottom, left = false, top = true, clockWise = true, radius = corner) else lineTo(closeRight, closeBottom)
                if (bottomLeft) corner(popupLeft, popupBottom, left = true, top = false, clockWise = false, radius = corner) else lineTo(popupLeft, popupBottom)
                if (bottomRight) corner(popupRight, popupBottom, left = false, top = false, clockWise = false, radius = corner) else lineTo(popupRight, popupBottom)
                if (topRight) corner(popupRight, popupTop, left = false, top = true, clockWise = false, radius = corner) else lineTo(popupRight, popupTop)
                if (topLeft) corner(popupLeft, popupTop, left = true, top = true, clockWise = false, radius = corner) else lineTo(popupLeft, popupTop)
            }
        }
        close()
    }

    private class ChildrenOutlineProvider(
        private val corner: Float,
        private val ground: Ground,
        private val strokeWidth: Int,
    ) : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val (left, right, bottom) = when (ground) {
                Ground.Left -> Triple(0, strokeWidth, strokeWidth)
                Ground.Right -> Triple(strokeWidth, 0, strokeWidth)
                Ground.Bottom -> Triple(strokeWidth, strokeWidth, 0)
            }
            outline.setRoundRect(left, strokeWidth, view.width - right, view.height - bottom, corner)
        }
    }
}
