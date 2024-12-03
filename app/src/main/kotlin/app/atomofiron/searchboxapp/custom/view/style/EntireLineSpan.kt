package app.atomofiron.searchboxapp.custom.view.style

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.LineBackgroundSpan

class EntireLineSpan(
    private val backgroundColor: Int,
    private val textColor: Int,
    private val cornerRadius: Float
) : LineBackgroundSpan {
    private val rect = RectF()

    override fun drawBackground(
        convas: Canvas,
        paint: Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lnum: Int,
    ) {
        text ?: return

        val width = paint.measureText(text, start, end)
        rect.set(left.toFloat(), top.toFloat(), left + width, bottom.toFloat())

        paint.color = backgroundColor
        convas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
        paint.color = textColor
    }
}
