package app.atomofiron.searchboxapp.custom.view.dangerous

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import androidx.core.graphics.ColorUtils

class DangerousCircles(private val colorr: Int) {
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = colorr
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
        first = second - width / 16f
        third = second + width / 16f
        maxOffset = width / 2 + (third - first)
        y = (bottom - top) / 2f
    }

    fun drawStairs(canvas: Canvas, centerX: Float) {
        val minRadius = y
        val second = minRadius + (width - centerX) / 2
        val first = minRadius + (second - minRadius) / 2
        val third = second + (second - first)
        val saved = canvas.save()
        canvas.drawColor(ColorUtils.setAlphaComponent(paint.color, 0x20))
        paint.alpha = 0x20
        canvas.drawCircle(0f, y, first, paint)
        paint.alpha = 0x20
        canvas.drawCircle(0f, y, second, paint)
        paint.alpha = 0x20
        canvas.drawCircle(0f, y, third, paint)
        canvas.restoreToCount(saved)
    }

    fun draw(canvas: Canvas, centerX: Float) {
        val minRadius = y
        val dr = minRadius * centerX / width
        val radius = width - centerX + dr
        if (radius <= 0) return
        val start = minRadius / radius
        val part = (radius - minRadius) / 4 / radius
        val colors = intArrayOf(0, colorr, 0, colorr, 0, colorr, 0, colorr)
        val stops = floatArrayOf(0f, start + part, start + part, start + part * 2, start + part * 2, start + part * 3, start + part * 3, 1f)
        paint.alpha = 0x10
        paint.shader = RadialGradient(0f, y, radius, colors, stops, Shader.TileMode.REPEAT)
        val saved = canvas.save()
        canvas.drawColor(ColorUtils.setAlphaComponent(paint.color, 0x20))
        canvas.translate(centerX, 0f)
        canvas.drawCircle(0f, y, radius, paint)
        canvas.restoreToCount(saved)
    }
}
