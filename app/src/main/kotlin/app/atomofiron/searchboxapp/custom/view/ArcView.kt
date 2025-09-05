package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.withTranslation
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.utils.Alpha
import com.google.android.material.textview.MaterialTextView
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class ArcView : MaterialTextView {
    companion object {
        private const val STEP_MIN = 0.001f
        private const val STEP_MAX = 0.01f
    }

    private val colorProgress = context.findColorByAttr(MaterialAttr.colorPrimary)
    private val colorTrack = ColorUtils.setAlphaComponent(colorProgress, Alpha.LEVEL_30)
    private val strokeWidth = resources.getDimension(R.dimen.arc_stroke_width)

    private val paint = Paint()
    private val rect = RectF()
    private var targetProgress = 0f
    private var progress = 0f
    private var arcDegrees = 0f
    private var startDegrees = 0f

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
        clipToOutline = false
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        updateDegrees()
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

        updateDegrees()
    }

    private fun updateDegrees() {
        val textPaint = super.paint
        val descent = textPaint.descent()
        val textWidth = textPaint.measureText(text, 0, text.length)
        val textTop = baseline + descent - textPaint.textSize
        val textHeight = height - textTop
        val holeLength = textWidth + textHeight
        val diameter = min(width, height) - strokeWidth
        val circle = PI * diameter
        val arcLength = circle - holeLength
        val arc = 2 * PI * arcLength / circle
        arcDegrees = Math.toDegrees(arc).toFloat()
        startDegrees = 270 - arcDegrees / 2
    }

    override fun onDraw(canvas: Canvas) {
        canvas.withTranslation(y = super.paint.descent()) {
            super.onDraw(canvas)
        }
        paint.color = colorTrack
        canvas.drawArc(rect, startDegrees, arcDegrees, false, paint)
        paint.color = colorProgress
        canvas.drawArc(rect, startDegrees, arcDegrees * progress, false, paint)
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