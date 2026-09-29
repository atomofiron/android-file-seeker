package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout

open class TextFieldBox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : TextInputLayout(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wrapSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        super.onMeasure(widthMeasureSpec, wrapSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        when (val actual = top + measuredHeight) {
            bottom -> super.onLayout(changed, left, top, right, bottom)
            else -> super.layout(left, top, right, actual)
        }
    }
}
