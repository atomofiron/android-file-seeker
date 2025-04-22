package app.atomofiron.searchboxapp.custom.view.dock.item

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewOutlineProvider
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.extension.corner
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemDockBinding
import app.atomofiron.searchboxapp.custom.drawable.PathDrawable
import app.atomofiron.searchboxapp.custom.view.dock.DockBarView
import app.atomofiron.searchboxapp.custom.view.dock.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.DockItemHolder
import app.atomofiron.searchboxapp.custom.view.dock.DockMode
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

private const val DURATION = 512L
private const val DELAY = 256L
private const val COLLAPSED = 0f
private const val EXPANDED = 1f

// todo on the first and last item
// todo support sides

@SuppressLint("ViewConstructor")
class DockItemChildrenView(
    context: Context,
    children: List<DockItem>,
    private val itemWidth: Int,
    private val itemHeight: Int,
    private val selectListener: (DockItem) -> Unit,
) : FrameLayout(context), ValueAnimator.AnimatorUpdateListener {

    private val holder = LayoutInflater.from(context)
        .let { ItemDockBinding.inflate(it, this, true) }
        .let { DockItemHolder(it) { collapse() } }
    private val childrenView = DockBarView(context, mode = DockMode.Children(itemWidth, itemHeight, 2))
    private val backgroundPath = Path()
    private val corner = resources.getDimension(R.dimen.dock_overlay_corner)
    private val offset = resources.getDimension(R.dimen.dock_item_half_margin).roundToInt().toFloat()
    private val animator = ValueAnimator.ofFloat(COLLAPSED)
    private var currentValue = COLLAPSED
    private var targetValue = COLLAPSED
    private val clipPath = Path()

    init {
        setWillNotDraw(false)
        holder.bind(DockItem(R.drawable.ic_cross, 0))
        holder.itemView.setBackgroundColor(context.findColorByAttr(MaterialAttr.colorSurfaceContainer))
        holder.itemView.setOnClickListener { toggle() }
        holder.itemView.elevation = resources.getDimension(R.dimen.overlay_elevation)
        holder.itemView.background = PathDrawable(backgroundPath, context.findColorByAttr(MaterialAttr.colorSurfaceContainer))
        holder.itemView.updateLayoutParams {
            width = itemWidth
            height = itemHeight
        }
        childrenView.elevation = resources.getDimension(R.dimen.overlay_elevation)
        childrenView.outlineProvider = OutlineProvider(corner)
        childrenView.clipToOutline = true
        childrenView.submit(children)
        childrenView.setListener(::onSelect)
        addView(childrenView)
        childrenView.updateLayoutParams {
            width = WRAP_CONTENT
            height = WRAP_CONTENT
        }
        animator.addUpdateListener(this)
        animator.interpolator = DecelerateInterpolator()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        return super.dispatchTouchEvent(event).also {
            if (!it) remove()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        childrenView.translationY = -offset - childrenView.height
        childrenView.translationX = (width - childrenView.width) / 2f
        val width = width.toFloat()
        val height = height.toFloat()
        backgroundPath.reset()
        backgroundPath.corner(-corner, 0f, left = false, top = true, clockWise = true, radius = corner, offsetX = -offset, offsetY = -offset)
        backgroundPath.corner(0f, height - corner, left = true, top = false, clockWise = false, radius = corner, offsetX = -offset, offsetY = offset)
        backgroundPath.corner(width - corner, height, left = false, top = false, clockWise = false, radius = corner, offsetX = offset, offsetY = offset)
        backgroundPath.corner(width, corner, left = true, top = true, clockWise = true, radius = corner, offsetX = offset, offsetY = -offset)
        backgroundPath.close()
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        currentValue = animation.animatedValue as Float
        if (currentValue == COLLAPSED && targetValue == COLLAPSED) {
            remove()
        } else {
            val width = childrenView.width + itemWidth.toFloat() * 0
            val height = childrenView.height + itemHeight.toFloat()
            val radius = currentValue * sqrt(width * width + height * height)
            clipPath.reset()
            clipPath.addCircle(this.width / 2f, this.height / 2f, radius, Path.Direction.CW)
            invalidate()
            invalidateOutline()
        }
    }

    override fun draw(canvas: Canvas) {
        if (currentValue != targetValue) {
            canvas.clipPath(clipPath)
        }
        super.draw(canvas)
    }

    fun expand() = animTo(EXPANDED)

    private fun collapse(withDelay: Boolean = false) = animTo(COLLAPSED, withDelay)

    private fun toggle() {
        when (targetValue) {
            COLLAPSED -> expand()
            EXPANDED -> collapse()
        }
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

    private class OutlineProvider(private val corner: Float) : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) = outline.setRoundRect(0, 0, view.width, view.height, corner)
    }
}
