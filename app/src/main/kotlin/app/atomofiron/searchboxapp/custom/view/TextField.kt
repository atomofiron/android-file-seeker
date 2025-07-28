package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.graphics.Rect
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updatePaddingRelative
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.extension.debugRequireNotNull
import app.atomofiron.fileseeker.R
import com.google.android.material.textfield.TextInputLayout

@Suppress("LeakingThis")
open class TextField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AutoHideKeyboardField(context, attrs), TextWatcher, TextView.OnEditorActionListener {

    private var listeners = mutableListOf<OnSubmitListener>()

    var submitted: CharSequence = ""
        private set

    init {
        isSingleLine = true
        isLongClickable = false
        hideKeyboardOnDetached = false
        addTextChangedListener(this)
        imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        inputType = InputType.TYPE_TEXT_VARIATION_NORMAL or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        setRawInputType(InputType.TYPE_TEXT_VARIATION_NORMAL or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
        if (Android.O) {
            inputType = inputType or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            setRawInputType(inputType)
            setAutofillHints(null)
            importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }
        super.setOnEditorActionListener(this)
    }

    override fun setOnEditorActionListener(l: OnEditorActionListener?) = throw UnsupportedOperationException()

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        submitted = text ?: ""
    }

    open fun addOnSubmitListener(listener: OnSubmitListener) {
        if (listener !in listeners) {
            listeners.add(listener)
        }
    }

    open fun onCheck(value: String) = true

    open fun onSubmit(value: String) = Unit

    override fun isSuggestionsEnabled(): Boolean = false

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) = Unit
    override fun afterTextChanged(editable: Editable) = Unit

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            val value = text.toString()
            if (onCheck(value) && listeners.all { it.onCheck(value) }) {
                submitted = value
                clearFocus()
                onSubmit(value)
                listeners.forEach { it.onSubmit(value) }
            } else {
                return true
            }
        }
        return false
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (!focused && (imeOptions and EditorInfo.IME_ACTION_DONE != 0)) {
            setText(submitted, BufferType.NORMAL)
        }
    }

    override fun performClick(): Boolean {
        requestFocus()
        return super.performClick()
    }

    interface OnSubmitListener {
        fun onCheck(value: String) = true
        fun onSubmit(value: String)
    }
}

fun TextInputLayout.showError(show: Boolean) {
    errorIconDrawable = null
    error = resources.takeIf { show }?.getString(R.string.wrong_value)
    isErrorEnabled = error != null
    isHelperTextEnabled = error != null
    if (show) getChildAt(1)
        ?.let { indicatorArea -> indicatorArea as? LinearLayout }
        ?.updatePaddingRelative(end = 0)
        .debugRequireNotNull()
}
