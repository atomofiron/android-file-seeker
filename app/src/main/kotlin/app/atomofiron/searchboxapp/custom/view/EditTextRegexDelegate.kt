package app.atomofiron.searchboxapp.custom.view

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

private const val UNKNOWN = -1
private const val ZERO_CHAR = 0.toChar()

fun EditText.makeRegex() = addTextChangedListener(EditTextRegexDelegate(this))

private class EditTextRegexDelegate(private val editText: EditText) : TextWatcher {

    private var locked = false
    private var deleted = ZERO_CHAR
    private var start = 0
    private var count = 0

    private val openBrackets = charArrayOf('[', '{', '(')
    private val closeBrackets = charArrayOf(']', '}', ')')

    override fun beforeTextChanged(sequence: CharSequence, start: Int, count: Int, after: Int) {
        deleted = if (count == 1) sequence[start] else ZERO_CHAR
    }

    override fun onTextChanged(sequence: CharSequence, start: Int, before: Int, count: Int) {
        this.start = start
        this.count = count
    }

    override fun afterTextChanged(editable: Editable) {
        if (locked) return
        locked = true

        if (editable.length > start && deleted in openBrackets && editable[start] in closeBrackets) {
            editable.delete(start, start.inc())
        } else if (count == 1) {
            val position = start.inc()
            val char = editable[start]
            val index = openBrackets.indexOf(char)
            if (index != UNKNOWN) {
                editable.insert(position, closeBrackets[index].toString())
                editText.setSelection(start)
            }
        }
        locked = false
    }
}