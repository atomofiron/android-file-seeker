package app.atomofiron.searchboxapp.custom.view.dangerous

import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import android.widget.TextView
import app.atomofiron.searchboxapp.utils.isRtl

class DangerousThumbSpan(
    private val view: TextView,
    colorStart: Int,
    colorEnd: Int,
) : CharacterStyle(), UpdateAppearance {

    private val isRtl = view.isRtl()
    private val colors = if (isRtl) intArrayOf(colorEnd, colorStart) else intArrayOf(colorStart, colorEnd)
    private val stops = floatArrayOf(0f, 1f)
    var progress = 0f
        set(value) {
            field = value
            view.invalidate()
        }

    private var loop = false
    override fun updateDrawState(paint: TextPaint) {
        val layout = view.layout ?: return
        if (loop) return else loop = true
        val left = layout.getLineLeft(0)
        val right = layout.getLineRight(0)
        loop = false

        val width = right - left
        val start = if (isRtl) -width else width
        val end = -start
        val offset = start + (end - start) * progress

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(offset, 0f, offset + width, 0f, colors, stops, Shader.TileMode.CLAMP)
    }
}
