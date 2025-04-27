package app.atomofiron.searchboxapp.custom.view.dock.popup

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewOutlineProvider
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.graphics.withClip
import androidx.core.view.updateLayoutParams
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.coerceInRange
import app.atomofiron.common.util.extension.corner
import app.atomofiron.common.util.extension.nearby
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemDockBinding
import app.atomofiron.searchboxapp.custom.drawable.PathDrawable
import app.atomofiron.searchboxapp.custom.view.dock.DockViewImpl
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemChildren
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemConfig
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemHolder
import app.atomofiron.searchboxapp.custom.view.dock.DockMode
import app.atomofiron.searchboxapp.model.Layout.Ground
import app.atomofiron.searchboxapp.utils.Alpha
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val DURATION = 512L
private const val DELAY = 256L
private const val COLLAPSED = 0f
private const val EXPANDED = 1f

private val CrossItem = DockItem(DockItem.Id.Undefined, DockItem.Icon(R.drawable.ic_cross))

@SuppressLint("ViewConstructor")
class DockItemChildrenView(
    context: Context,
    children: DockItemChildren,
    private val config: DockItemConfig,
    private val selectListener: (DockItem) -> Unit,
) : FrameLayout(context), ValueAnimator.AnimatorUpdateListener {

    private val close = ItemDockBinding.inflate(LayoutInflater.from(context), this, true)
    private val childrenView = DockViewImpl(context, children, config, mode = DockMode.Popup(config.popup, children.columns))
    private val backgroundPath = Path()
    private var clipPath = Path()
    private var combinedPath = Path()
    private val corner = resources.getDimension(R.dimen.dock_overlay_corner)
    private val offset = resources.getDimension(R.dimen.dock_item_half_margin).roundToInt().toFloat()
    private val animator = ValueAnimator.ofFloat(COLLAPSED)
    private var currentValue = COLLAPSED
    private var targetValue = COLLAPSED

    init {
        setWillNotDraw(false)
        DockItemHolder(close) { collapse() }
            .bind(CrossItem, config)
        close.root.setOnClickListener { collapse() }
        close.root.updateLayoutParams {
            width = config.width
            height = config.height
        }
        childrenView.outlineProvider = ChildrenOutlineProvider(corner)
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
        animator.interpolator = AccelerateDecelerateInterpolator()

        elevation = resources.getDimension(R.dimen.overlay_elevation)
        background = PathDrawable(combinedPath, context.findColorByAttr(MaterialAttr.colorSurfaceContainer))
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
        var radius = childrenView.run { sqrt(width * width + height * height.toFloat()) }
        radius += config.run { sqrt(width * width + height * height.toFloat()) / 2 }
        radius *= currentValue
        combinedPath.set(backgroundPath)
        if (currentValue > COLLAPSED && currentValue < EXPANDED) {
            clipPath.reset()
            clipPath.addCircle(centerX, centerY, radius, Path.Direction.CW)
            combinedPath.op(backgroundPath, clipPath, Path.Op.INTERSECT)
        }
        // fixes shadow offset
        combinedPath.addRect(centerX, centerY + radius.dec(), centerX.inc(), centerY + radius, Path.Direction.CW)
        invalidate()
        invalidateOutline()

        val cross = (currentValue * 3 - 1).coerceInRange(Alpha.INVISIBLE, Alpha.VISIBLE)
        close.icon.alpha = cross
        close.icon.rotation = 45 * (cross - 1f)
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

    private fun collapse(withDelay: Boolean = false) = when {
        targetValue == COLLAPSED && !withDelay -> remove()
        else -> animTo(COLLAPSED, withDelay)
    }

    private fun remove() {
        animator.cancel()
        (parent as ViewGroup).removeView(this)
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
        val closeLeft = close.root.x - offset
        val closeTop = close.root.y - offset
        val closeRight = closeLeft + config.width + offset * 2
        val closeBottom = closeTop + config.height + offset * 2
        val popupLeft = childrenView.x
        val popupTop = childrenView.y
        val popupRight = popupLeft + childrenView.width
        val popupBottom = popupTop + childrenView.height
        val distance = corner * 2
        val topLeft = !nearby(distance, popupLeft, popupTop, closeRight, closeTop)
        val topRight = !nearby(distance, popupRight, popupTop, closeLeft, closeTop)
        val bottomRight = !nearby(distance, popupRight, popupBottom, closeRight, closeTop, closeLeft, closeBottom)
        val bottomLeft = !nearby(distance, popupLeft, popupBottom, closeLeft, closeTop, closeRight, closeBottom)
        reset()
        moveTo(closeLeft, closeTop)
        when (ground) {
            Ground.Bottom -> {
                if (bottomLeft) corner(closeLeft, closeTop, left = false, top = true, clockWise = false, radius = corner) else lineTo(closeLeft, closeTop)
                if (bottomRight) corner(closeRight, closeTop, left = true, top = true, clockWise = false, radius = corner) else lineTo(closeRight, closeTop)
                corner(closeRight, closeBottom, left = false, top = false, clockWise = true, radius = corner)
                corner(closeLeft, closeBottom, left = true, top = false, clockWise = true, radius = corner)
            }
            Ground.Right -> {
                if (topRight) corner(closeLeft, closeTop, left = true, top = false, clockWise = false, radius = corner) else lineTo(closeLeft, closeTop)
                corner(closeRight, closeTop, left = false, top = true, clockWise = true, radius = corner)
                corner(closeRight, closeBottom, left = false, top = false, clockWise = true, radius = corner)
                if (bottomRight) corner(closeLeft, closeBottom, left = true, top = true, clockWise = false, radius = corner) else lineTo(closeLeft, closeBottom)
            }
            Ground.Left -> {
                corner(closeLeft, closeTop, left = true, top = true, clockWise = true, radius = corner)
                if (topLeft) corner(closeRight, closeTop, left = false, top = false, clockWise = false, radius = corner) else lineTo(closeRight, closeTop)
                if (bottomLeft) corner(closeRight, closeBottom, left = false, top = true, clockWise = false, radius = corner) else lineTo(closeRight, closeBottom)
                corner(closeLeft, closeBottom, left = true, top = false, clockWise = true, radius = corner)
            }
        }
        close()
        moveTo(popupLeft, popupTop)
        if (topLeft) corner(popupLeft, popupTop, left = true, top = true, clockWise = true, radius = corner) else lineTo(popupLeft, popupTop)
        if (topRight) corner(popupRight, popupTop, left = false, top = true, clockWise = true, radius = corner) else lineTo(popupRight, popupTop)
        if (bottomRight) corner(popupRight, popupBottom, left = false, top = false, clockWise = true, radius = corner) else lineTo(popupRight, popupBottom)
        if (bottomLeft) corner(popupLeft, popupBottom, left = true, top = false, clockWise = true, radius = corner) else lineTo(popupLeft, popupBottom)
        close()
    }

    private class ChildrenOutlineProvider(private val corner: Float) : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) = outline.setRoundRect(0, 0, view.width, view.height, corner)
    }
}
