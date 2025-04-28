package app.atomofiron.searchboxapp.custom.view.layout

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import lib.atomofiron.insets.InsetsProvider
import lib.atomofiron.insets.InsetsProviderImpl

@Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE")
class RootFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr),
    InsetsProvider by InsetsProviderImpl(),
    MeasureProvider by MeasureProviderImpl()
{
    override val view get() = this

    init {
        onInit()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        onPreMeasure(widthMeasureSpec, heightMeasureSpec)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}