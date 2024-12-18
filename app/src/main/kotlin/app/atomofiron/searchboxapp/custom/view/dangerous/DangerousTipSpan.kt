package app.atomofiron.searchboxapp.custom.view.dangerous

import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.text.TextPaint
import android.text.style.CharacterStyle
import android.text.style.UpdateAppearance
import android.widget.TextView
import app.atomofiron.searchboxapp.utils.isRtl

class DangerousTipSpan(private val view: TextView) : CharacterStyle(), UpdateAppearance {

    private val isRtl = view.isRtl()
    private val colors = IntArray(3)
    private val stops = floatArrayOf(0f, 0.5f, 1f)
    var progress = 0f
        set(value) {
            field = if (isRtl) (1f - value) else value
            view.invalidate()
        }

    private var loop = false
    override fun updateDrawState(paint: TextPaint) {
        val layout = view.layout ?: return
        if (loop) return else loop = true
        var left = layout.getLineLeft(0)
        var right = layout.getLineRight(0)
        loop = false

        val offset = (right - left) * (progress * 2 - 1)
        left += offset
        right += offset
        colors[1] = paint.color

        paint.style = Paint.Style.FILL
        paint.shader = LinearGradient(left, 0f, right, 0f, colors, stops, Shader.TileMode.CLAMP)
    }
}