package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import androidx.core.graphics.ColorUtils
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.MaterialAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.utils.Alpha
import com.google.android.material.textview.MaterialTextView
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class ArcView : MaterialTextView {
    companion object {
        private const val STEP_MIN = 0.001f
        private const val STEP_MAX = 0.01f
    }

    private var progress = 0f
    private var targetProgress = 0f
    private val rect = RectF()
    private val paint = Paint()

    private val colorProgress = context.findColorByAttr(MaterialAttr.colorPrimary)
    private val colorTrack = ColorUtils.setAlphaComponent(colorProgress, Alpha.Level30)
    private val strokeWidth = resources.getDimension(R.dimen.arc_stroke_width)
    private val strokeMargin = strokeWidth * 2

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int) : super(context, attrs, defStyleRes) {
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = context.findColorByAttr(MaterialAttr.colorPrimary)
        paint.strokeWidth = strokeWidth

        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val offset = strokeWidth / 2
        rect.set(0f, 0f, width.toFloat(), height.toFloat())
        val width = rect.width()
        val height = rect.height()
        var dif = max(0f, height - width)
        rect.top += dif + offset
        rect.bottom -= dif + offset
        dif = max(0f, width - height)
        rect.left += dif + offset
        rect.right -= dif + offset
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val textPaint = getPaint()
        val textWidth = textPaint.measureText(text, 0, text.length)
        val top = baseline + textPaint.descent() - textPaint.textSize
        val textHeight = baseline - top
        val radius = (height - strokeWidth) / 2
        val minArc = PI + 2 * asin((height / 2 - textHeight - strokeMargin) / radius)
        val arc = max(minArc, PI * 2 - (textWidth + 2 * strokeMargin) / radius)
        val maxArcDegrees = Math.toDegrees(arc).toFloat()
        val part = (maxArcDegrees - 180) / 2
        val start = 180 - part
        val arcDegrees = maxArcDegrees * progress

        paint.color = colorTrack
        canvas.drawArc(rect, start, maxArcDegrees, false, paint)
        paint.color = colorProgress
        canvas.drawArc(rect, start, arcDegrees, false, paint)
        if (progress != targetProgress) {
            var dif = targetProgress - progress
            val sign = dif.sign
            dif = abs(dif) / 8
            dif = max(STEP_MIN, dif)
            dif = min(STEP_MAX, dif) * sign
            progress = min(targetProgress, progress + dif)
            invalidate()
        }
    }

    fun set(progress: Long, max: Long) {
        targetProgress = if (max == 0L) 0f else progress / max.toFloat()
        invalidate()
    }
}