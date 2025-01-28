package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.drawable.BallsDrawable.Companion.setBallsDrawable

class BallsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val drawable = setBallsDrawable()

    init {
        val styled = context.obtainStyledAttributes(attrs, R.styleable.BallsView, defStyleAttr, 0)
        val oneBall = styled.getBoolean(R.styleable.BallsView_oneBall, true)
        styled.recycle()

        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    fun setColor(color: Int) = drawable.setColor(color)
}