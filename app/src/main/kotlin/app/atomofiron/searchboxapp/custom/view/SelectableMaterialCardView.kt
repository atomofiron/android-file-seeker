package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.MaterialAttr
import app.atomofiron.fileseeker.R
import com.google.android.material.card.MaterialCardView

class SelectableMaterialCardView : MaterialCardView {

    private val framePaint = Paint()
    private val innerRect = RectF()
    private val outerRect = RectF()
    private val frameWidth = resources.getDimension(R.dimen.selected_stroke_width)
    private val framePath = Path()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        framePaint.isAntiAlias = true
        framePaint.style = Paint.Style.FILL
        framePaint.color = context.findColorByAttr(MaterialAttr.colorPrimary)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) updateFramePath()
    }

    override fun setRadius(radius: Float) {
        super.setRadius(radius)
        updateFramePath()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        // draw over the card outline
        if (isSelected) canvas.drawPath(framePath, framePaint)
    }

    private fun updateFramePath() {
        outerRect.right = width.toFloat()
        outerRect.bottom = height.toFloat()
        innerRect.left = frameWidth
        innerRect.top = frameWidth
        innerRect.right = outerRect.right - frameWidth
        innerRect.bottom = outerRect.bottom - frameWidth
        val innerRadius = radius - frameWidth
        framePath.reset()
        framePath.addRect(outerRect, Path.Direction.CW)
        framePath.addRoundRect(innerRect, innerRadius, innerRadius, Path.Direction.CCW)
        framePath.close()
    }
}