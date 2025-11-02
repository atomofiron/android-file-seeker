package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.drawable.MuonsDrawable.Companion.setMuonsDrawable
import androidx.core.content.withStyledAttributes
import app.atomofiron.searchboxapp.custom.drawable.MuonsDrawable

class MuonsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private lateinit var drawable: MuonsDrawable

    init {
        context.withStyledAttributes(attrs, R.styleable.MuonsView, defStyleAttr, 0) {
            val fillCenter = getBoolean(R.styleable.MuonsView_fillCenter, true)
            drawable = setMuonsDrawable(fillCenter)
        }
    }

    fun setTint(color: Int) {
        imageTintList = ColorStateList.valueOf(color)
    }

    fun setSpeed(speed: MuonsDrawable.Speed) = drawable.setSpeed(speed)
}