package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo

open class TextField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AutoHideKeyboardField(context, attrs), TextWatcher {
    private var onSubmitListener: OnSubmitListener? = null

    private var submittedValue: CharSequence = ""

    init {
        hideKeyboardOnDetached = false
        addTextChangedListener(this)
        imeOptions = imeOptions or EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS or EditorInfo.IME_ACTION_DONE
        inputType = inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        isSingleLine = true
        isLongClickable = false
        setHintTextColor(0)
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)

        submittedValue = text ?: ""
    }

    open fun setOnSubmitListener(listener: OnSubmitListener?) {
        onSubmitListener = listener
    }

    open fun onSubmit(value: String) {
        onSubmitListener?.onSubmit(value)
    }

    override fun isSuggestionsEnabled(): Boolean = false

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
    override fun afterTextChanged(editable: Editable) = Unit

    override fun onEditorAction(actionCode: Int) {
        super.onEditorAction(actionCode)
        if (actionCode == EditorInfo.IME_ACTION_DONE) {
            submittedValue = text.toString()
            onSubmit(text.toString())
            clearFocus()
        }
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (!focused) {
            setText(submittedValue, BufferType.NORMAL)
        }
    }

    override fun performClick(): Boolean {
        requestFocus()
        return super.performClick()
    }

    fun interface OnSubmitListener {
        fun onSubmit(value: String)
    }
}