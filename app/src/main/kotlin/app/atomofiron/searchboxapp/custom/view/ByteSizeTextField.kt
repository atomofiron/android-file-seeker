package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.util.AttributeSet
import android.view.Gravity
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.utils.convert
import kotlin.math.min

class ByteSizeTextField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : TextField(context, attrs) {
    companion object {
        private val startingZeros = Regex("^0+(?=\\d)")
    }

    private var submitListener: OnSubmitListener? = null
    private val suffixes = resources.getStringArray(R.array.size_suffix_arr)

    init {
        filters = arrayOf<InputFilter>(InputFilterImpl())
        gravity = Gravity.CENTER_HORIZONTAL
        hint = "_____"
        inputType = inputType and InputType.TYPE_NUMBER_FLAG_DECIMAL.inv()
    }

    fun setValue(value: Int) = setText(value.convert(suffixes))

    fun setOnSubmitListener(listener: OnSubmitListener?) {
        submitListener = listener
    }

    override fun onSubmit(value: String) {
        submitListener?.onSubmit(value.convert())
    }

    override fun afterTextChanged(editable: Editable) {
        val selection = selectionStart
        val withoutStartingZero = editable.replace(startingZeros, "")
        if (withoutStartingZero != editable.toString()) {
            setText(withoutStartingZero)
            setSelection(min(selection, withoutStartingZero.length))
        }
    }

    fun interface OnSubmitListener {
        fun onSubmit(value: Int)
    }

    private class InputFilterImpl : InputFilter {

        private val regex = Regex("(\\d+|0)([gGгГ]|[mMмМ]|[kKкК])?[bBбБ]?")

        override fun filter(
            source: CharSequence?,
            start: Int,
            end: Int,
            dest: Spanned?,
            dstart: Int,
            dend: Int
        ): CharSequence {
            source ?: return ""
            val destination = dest ?: ""
            val result = destination.replaceRange(dstart, dend, source.substring(start, end))
            return when {
                result.isEmpty() -> "0"
                !result.matches(regex) -> ""
                else -> source
            }
        }
    }
}