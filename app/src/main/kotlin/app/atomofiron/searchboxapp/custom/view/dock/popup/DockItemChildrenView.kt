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
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.graphics.withClip
import androidx.core.view.updateLayoutParams
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.extension.corner
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemDockBinding
import app.atomofiron.searchboxapp.custom.drawable.PathDrawable
import app.atomofiron.searchboxapp.custom.view.dock.DockViewImpl
import app.atomofiron.searchboxapp.custom.view.dock.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.DockItemChildren
import app.atomofiron.searchboxapp.custom.view.dock.DockItemConfig
import app.atomofiron.searchboxapp.custom.view.dock.DockItemHolder
import app.atomofiron.searchboxapp.custom.view.dock.DockMode
import app.atomofiron.searchboxapp.model.Layout.Ground
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val DURATION = 512L
private const val DELAY = 256L
private const val COLLAPSED = 0f
private const val EXPANDED = 1f

@SuppressLint("ViewConstructor")
class DockItemChildrenView(
    context: Context,
    children: DockItemChildren,
    private val config: DockItemConfig,
    private val selectListener: (DockItem) -> Unit,
) : FrameLayout(context), ValueAnimator.AnimatorUpdateListener {

    private val holder = LayoutInflater.from(context)
        .let { ItemDockBinding.inflate(it, this, true) }
        .let { DockItemHolder(it) { collapse() } }
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
        holder.bind(DockItem(R.drawable.ic_cross, 0), config)
        holder.itemView.setOnClickListener { toggle() }
        holder.itemView.updateLayoutParams {
            width = config.width
            height = config.height
        }
        childrenView.outlineProvider = ChildrenOutlineProvider(corner)
        childrenView.clipToOutline = true
        childrenView.background = null
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

    private fun collapse(withDelay: Boolean = false) = animTo(COLLAPSED, withDelay)

    private fun toggle() = when (targetValue) {
        COLLAPSED -> expand()
        EXPANDED -> collapse()
        else -> Unit
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
        var left = holder.itemView.x
        var top = holder.itemView.y
        var right = left + config.width
        var bottom = top + config.height
        reset()
        when (config.popup.ground) {
            Ground.Bottom -> {
                corner(left, top, left = false, top = true, clockWise = true, radius = corner, offsetX = -offset, offsetY = -offset)
                corner(left, bottom, left = true, top = false, clockWise = false, radius = corner, offsetX = -offset, offsetY = offset)
                corner(right, bottom, left = false, top = false, clockWise = false, radius = corner, offsetX = offset, offsetY = offset)
                corner(right, top, left = true, top = true, clockWise = true, radius = corner, offsetX = offset, offsetY = -offset)
            }
            Ground.Right -> {
                corner(left, bottom, left = true, top = true, clockWise = true, radius = corner, offsetX = -offset, offsetY = offset)
                corner(right, bottom, left = false, top = false, clockWise = false, radius = corner, offsetX = offset, offsetY = offset)
                corner(right, top, left = false, top = true, clockWise = false, radius = corner, offsetX = offset, offsetY = -offset)
                corner(left, top, left = true, top = false, clockWise = true, radius = corner, offsetX = -offset, offsetY = -offset)
            }
            Ground.Left -> {
                corner(right, top, left = false, top = false, clockWise = true, radius = corner, offsetX = offset, offsetY = -offset)
                corner(left, top, left = true, top = true, clockWise = false, radius = corner, offsetX = -offset, offsetY = -offset)
                corner(left, bottom, left = true, top = false, clockWise = false, radius = corner, offsetX = -offset, offsetY = offset)
                corner(right, bottom, left = false, top = true, clockWise = true, radius = corner, offsetX = offset, offsetY = offset)
            }
        }
        close()
        left = childrenView.x
        top = childrenView.y
        right = left + childrenView.width
        bottom = top + childrenView.height
        moveTo(right - corner, bottom)
        corner(right, bottom, left = false, top = false, clockWise = false, radius = corner)
        corner(right, top, left = false, top = true, clockWise = false, radius = corner)
        corner(left, top, left = true, top = true, clockWise = false, radius = corner)
        corner(left, bottom, left = true, top = false, clockWise = false, radius = corner)
        close()
    }

    private class ChildrenOutlineProvider(private val corner: Float) : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) = outline.setRoundRect(0, 0, view.width, view.height, corner)
    }
}
