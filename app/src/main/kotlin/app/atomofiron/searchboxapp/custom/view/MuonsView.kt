package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.drawable.MuonsDrawable
import app.atomofiron.searchboxapp.custom.drawable.MuonsDrawable.Companion.setBallsDrawable

class MuonsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val drawable: MuonsDrawable

    init {
        val styled = context.obtainStyledAttributes(attrs, R.styleable.BallsView, defStyleAttr, 0)
        val fillCenter = styled.getBoolean(R.styleable.BallsView_fillCenter, true)
        drawable = setBallsDrawable(fillCenter)
        styled.recycle()

        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    fun setColor(color: Int) = drawable.setColor(color)

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        drawable.setVisible(visibility == View.VISIBLE, false)
    }
}