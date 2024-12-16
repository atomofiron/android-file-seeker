package app.atomofiron.searchboxapp.custom.view.style

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.RectF
import android.text.style.ReplacementSpan

class RoundedBackgroundSpan(
    private val backgroundColor: Int,
    private val borderColor: Int,
    private val textColor: Int,
    private val radius: Float,
    private val borderWidth: Float,
) : ReplacementSpan() {

    private val borderInset = borderWidth / 2
    private val rect = RectF()

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: FontMetricsInt?): Int {
        return paint.measureText(text.subSequence(start, end).toString()).toInt()
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val width = paint.measureText(text.subSequence(start, end).toString())
        rect.set(x, top.toFloat() + 3, x + width, bottom.toFloat() + 1)
        paint.color = backgroundColor
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.color = borderColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = borderWidth
        rect.inset(0f, borderInset)
        canvas.drawRoundRect(rect, radius, radius, paint)
        paint.color = textColor
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 0f
        canvas.drawText(text, start, end, x, y.toFloat(), paint)
    }
}