package app.atomofiron.searchboxapp.screens.curtain.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import androidx.core.graphics.ColorUtils
import app.atomofiron.common.util.findBooleanByAttr
import app.atomofiron.searchboxapp.MaterialAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.asOverlayOn
import app.atomofiron.searchboxapp.utils.getColorByAttr
import app.atomofiron.searchboxapp.utils.setColorAlpha

open class CurtainBackground(context: Context) : Drawable() {

    private val isBlackDeep = context.findBooleanByAttr(R.attr.isBlackDeep)
    private val cornerRadius = context.resources.getDimension(R.dimen.corner_extra_large)
    private val curtainColor = context.getColorByAttr(R.attr.colorBackground)
    private val strokeWidth = if (isBlackDeep) context.resources.getDimensionPixelSize(R.dimen.stroke_width) else 0
    private val dragHandleColor = ColorUtils.setAlphaComponent(context.getColorByAttr(MaterialAttr.colorOnSurfaceVariant), 102)
    private val dragHandleRect = RectF()
    private val dragHandleWidth = context.resources.getDimension(R.dimen.drag_handle_width)
    private val dragHandleMargin = context.resources.getDimension(R.dimen.drag_handle_margin)
    private val strokeColor = when {
        isBlackDeep -> context
            .getColorByAttr(MaterialAttr.strokeColor)
            .setColorAlpha(Alpha.Level50)
            .asOverlayOn(curtainColor)
        else -> Color.TRANSPARENT
    }
    private val paint = Paint()
    private val boundsF = RectF()
    private val outlineRect = Rect()
    private val borderRect = RectF()

    init {
        paint.isAntiAlias = true
        dragHandleRect.top = dragHandleMargin
        dragHandleRect.bottom = dragHandleMargin + context.resources.getDimension(R.dimen.drag_handle_height)
    }

    override fun setAlpha(alpha: Int) = Unit

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom + cornerRadius.toInt())
        boundsF.set(bounds)
        outlineRect.set(bounds)
        outlineRect.left -= strokeWidth
        outlineRect.right += strokeWidth
        borderRect.set(outlineRect)

        val center = (left + right) / 2
        dragHandleRect.left = center - dragHandleWidth / 2
        dragHandleRect.right =  center + dragHandleWidth / 2
    }

    override fun getOutline(outline: Outline) {
        outline.setRoundRect(outlineRect, cornerRadius)
        outline.alpha = 0f
    }

    override fun draw(canvas: Canvas) {
        var radius = cornerRadius

        paint.color = curtainColor
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(boundsF, radius, radius, paint)

        if (isBlackDeep) {
            paint.color = strokeColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = strokeWidth * 2f
            canvas.drawRoundRect(borderRect, radius, radius, paint)
        }

        paint.style = Paint.Style.FILL
        paint.color = dragHandleColor
        radius = dragHandleRect.height()
        canvas.drawRoundRect(dragHandleRect, radius, radius, paint)
    }
}