package app.atomofiron.searchboxapp.custom.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import androidx.core.graphics.withClip
import app.atomofiron.common.util.Android

class PathDrawable(
    private val path: Path,
    private val fill: Int,
    private val border: PathBorder? = null,
) : Drawable() {

    private val paint = Paint()

    init {
        paint.isAntiAlias = true
    }

    // needed for shadow to work
    override fun getOutline(outline: Outline) = when {
        Android.R -> outline.setPath(path)
        else -> Unit
    }

    override fun draw(canvas: Canvas) {
        if (fill != Color.TRANSPARENT) {
            paint.color = fill
            paint.style = Paint.Style.FILL
            canvas.drawPath(path, paint)
        }
        if (border != null) {
            paint.color = border.color
            paint.strokeWidth = 2 * border.width
            paint.style = Paint.Style.STROKE
            canvas.withClip(path) {
                canvas.drawPath(path, paint)
            }
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
