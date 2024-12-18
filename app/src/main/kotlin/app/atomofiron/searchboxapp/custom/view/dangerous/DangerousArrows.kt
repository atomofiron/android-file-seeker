package app.atomofiron.searchboxapp.custom.view.dangerous

import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.graphics.ColorUtils
import app.atomofiron.searchboxapp.utils.Alpha
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sqrt

private const val Quarter = (Math.PI / 2).toFloat()

class DangerousArrows(private val color: Int, arrowThickness: Float) {

    private val pts = FloatArray(8)
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        strokeWidth = arrowThickness
    }
    private var width = 0f
    private var maxOffset = 0f
    private var y = 0f
    private var first = 0f
    private var second = 0f
    private var third = 0f

    fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        width = (right - left).toFloat()
        second = left + width / 2f
        first = second + width / 16f
        third = second - width / 16f
        maxOffset = width / 2 + (first - third)
        y = (bottom - top) / 2f
    }

    fun draw(
        canvas: Canvas,
        flip: Boolean,
        progress: Float, // 0..1
        alpha: Float, // -1..1
        offset: Float,
        arrowSize: Float,
    ) {
        val appearing = alpha > 0
        val interpolatedAlpha = (abs(alpha * 3) - 2).coerceAtLeast(0f)
        val fastAlpha = interpolatedAlpha * interpolatedAlpha
        val slowAlpha = sqrt(interpolatedAlpha)
        val firstAlpha = (Alpha.VisibleInt * if (appearing) fastAlpha else slowAlpha).toInt()
        val secondAlpha = (Alpha.VisibleInt * interpolatedAlpha).toInt()
        val thirdAlpha = (Alpha.VisibleInt * if (appearing) slowAlpha else fastAlpha).toInt()
        val fullProgress = progress * 1.5f
        val firstOffset = maxOffset * fullProgress.interpolate(0f)
        val firstScale = firstOffset / maxOffset
        val first = first + firstOffset
        val secondOffset = maxOffset * fullProgress.interpolate(0.2f)
        val secondScale = secondOffset / maxOffset
        val second = second + secondOffset
        val thirdOffset = maxOffset * fullProgress.interpolate(0.4f)
        val thirdScale = thirdOffset / maxOffset
        val third = third + thirdOffset

        val saved = canvas.save()
        canvas.translate(offset, 0f)
        if (flip) {
            canvas.translate(width, 0f)
            canvas.scale(-1f, 1f)
        }
        paint.color = ColorUtils.setAlphaComponent(color, firstAlpha)
        canvas.translate(first, y)
        canvas.drawArrow(1 + firstScale, 1 - firstScale, arrowSize)
        paint.color = ColorUtils.setAlphaComponent(color, secondAlpha)
        canvas.translate(second - first, 0f)
        canvas.drawArrow(1 + secondScale, 1 - secondScale, arrowSize)
        paint.color = ColorUtils.setAlphaComponent(color, thirdAlpha)
        canvas.translate(third - second, 0f)
        canvas.drawArrow(1 + thirdScale, 1 - thirdScale, arrowSize)
        canvas.restoreToCount(saved)
    }

    private fun Float.interpolate(threshold: Float): Float {
        val moved = coerceIn(threshold..threshold.inc()) - threshold
        return (1f - cos(Quarter * moved))
    }

    private fun Canvas.drawArrow(scaleX: Float, scaleY: Float, arrowSize: Float) {
        val half = arrowSize / 2
        pts[0] = -half * scaleX
        pts[1] = -half * scaleY
        pts[6] = pts[0]
        pts[7] = -pts[1]
        drawLines(pts, paint)
    }
}

