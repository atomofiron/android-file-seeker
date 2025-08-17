package app.atomofiron.searchboxapp.custom.view.layout

import android.content.Context
import android.util.AttributeSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.withStyledAttributes
import app.atomofiron.fileseeker.R

class LimitedWidthCoordinatorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CoordinatorLayout(context, attrs, defStyleAttr) {

    private var maxWidth = Int.MAX_VALUE

    init {
        context.withStyledAttributes(attrs, R.styleable.LimitedWidthCoordinatorLayout, defStyleAttr, 0) {
            maxWidth = getDimensionPixelSize(R.styleable.LimitedWidthCoordinatorLayout_maxWidth, maxWidth)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec).coerceAtMost(maxWidth)
        val newWidthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.getMode(widthMeasureSpec))
        super.onMeasure(newWidthSpec, heightMeasureSpec)
    }
}