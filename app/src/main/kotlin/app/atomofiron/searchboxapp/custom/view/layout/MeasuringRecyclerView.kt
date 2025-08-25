package app.atomofiron.searchboxapp.custom.view.layout

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class MeasuringRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : RecyclerView(context, attrs, defStyleAttr), MeasureProvider by MeasureProviderImpl() {

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        onPreMeasure(widthSpec, heightSpec)
        super.onMeasure(widthSpec, heightSpec)
    }
}