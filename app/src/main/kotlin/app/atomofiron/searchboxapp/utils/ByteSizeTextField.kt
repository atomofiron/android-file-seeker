package app.atomofiron.searchboxapp.utils

import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.text.TextWatcher
import android.widget.EditText
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.TextField
import kotlin.math.min

private val startingZeros = Regex("^0+(?=\\d)")

class ByteSizeDelegate(
    private val editText: EditText,
    private val listener: (Int) -> Unit,
) : TextWatcher, InputFilter, TextField.OnSubmitListener {
    companion object {
        fun TextField.makeByteSize(listener: (Int) -> Unit) {
            val delegate = ByteSizeDelegate(this, listener)
            filters = arrayOf<InputFilter>(delegate)
            hint = "_____"
            inputType = inputType and InputType.TYPE_NUMBER_FLAG_DECIMAL.inv()
            addOnSubmitListener(delegate)
            addTextChangedListener(delegate)
        }
    }

    private val suffixes = editText.resources.getStringArray(R.array.size_suffix_arr)
    private val regex = Regex("(\\d+|0)([gGгГ]|[mMмМ]|[kKкК])?[bBбБ]?")
    private var submitted = -1

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int,
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

    override fun beforeTextChanged(value: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(value: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(editable: Editable) = editText.run {
        val string = editable.toString()
        val selection = selectionStart
        val withoutStartingZero = string.replace(startingZeros, "")
        if (withoutStartingZero != string) {
            setText(withoutStartingZero)
            setSelection(min(selection, withoutStartingZero.length))
        }
        if (!isFocused) try {
            submitted = string.convert()
            val converted = submitted.convert(suffixes)
            if (converted != string) {
                setText(converted)
            }
        } catch (e: NumberFormatException) {
        }
    }

    override fun onSubmit(value: String) = listener(value.convert())
}
