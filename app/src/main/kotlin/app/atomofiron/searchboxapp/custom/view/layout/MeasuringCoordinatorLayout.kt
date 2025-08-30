package app.atomofiron.searchboxapp.custom.view.layout

import android.content.Context
import android.util.AttributeSet
import androidx.coordinatorlayout.widget.CoordinatorLayout

class MeasuringCoordinatorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : CoordinatorLayout(context, attrs, defStyleAttr), MeasureProvider by MeasureProviderImpl() {

    override val view get() = this

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        onPreMeasure(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}