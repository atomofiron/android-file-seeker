package app.atomofiron.searchboxapp.utils

import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import app.atomofiron.common.util.extension.ctx
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.TextField
import app.atomofiron.searchboxapp.custom.view.showError
import com.google.android.material.textfield.TextInputLayout
import kotlin.math.min

private val startingZeros = Regex("^0+(?=\\d)")

fun TextField.makeByteSize(listener: (Int) -> Unit) {
    val delegate = ByteSizeDelegate(this, listener)
    filters += arrayOf<InputFilter>(delegate)
    inputType = inputType and InputType.TYPE_NUMBER_FLAG_DECIMAL.inv()
    addOnFocusChangeListener(delegate)
    addListener(delegate)
    addTextChangedListener(delegate)
}

class ByteSizeDelegate(
    private val textField: EditText,
    private val listener: (Int) -> Unit,
) : TextWatcher, InputFilter, TextField.Listener, View.OnFocusChangeListener {

    private val inputLayout = textField.parent.parent as? TextInputLayout

    private val suffixes = textField.resources.getStringArray(R.array.size_suffix_arr)
    private val regex = Regex("(\\d+|0)([gGгГ]|[mMмМ]|[kKкК])?[bBбБ]?")
    private var valid = 0

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

    override fun afterTextChanged(editable: Editable) = textField.ctx {
        val string = editable.toString()
        val selection = selectionStart
        val withoutStartingZero = string.replace(startingZeros, "")
        if (withoutStartingZero != string) {
            setText(withoutStartingZero)
            setSelection(min(selection, withoutStartingZero.length))
        }
        if (!isFocused) try {
            valid = string.convert()
            val converted = valid.convert(suffixes)
            if (converted != string) {
                setText(converted)
            }
        } catch (e: NumberFormatException) {
        } else {
            inputLayout?.showError(string.convertOrNull() == null)
        }
    }

    override fun onSubmitCheck(value: String): Boolean {
        return value.convertOrNull()?.let { valid = it } != null
    }

    override fun onSubmit(value: String) = listener(valid)

    override fun onFocusChange(view: View, hasFocus: Boolean) {
        if (!hasFocus) {
            textField.setText(valid.convert(suffixes))
            inputLayout?.showError(false)
        }
    }
}
