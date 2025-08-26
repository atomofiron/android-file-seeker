package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.updatePaddingRelative
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.extension.debugRequire
import app.atomofiron.common.util.extension.debugRequireNotNull
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.drawable.HybridTextLayoutDrawable
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_FILLED

@Suppress("LeakingThis")
open class TextField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : AutoHideKeyboardField(context, attrs), TextWatcher, TextView.OnEditorActionListener {

    private var listeners = mutableListOf<OnSubmitListener>()
    private var actionListener: OnEditorActionListener? = null
    private var filledDelegate: FilledDelegate? = null

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

    override fun setOnEditorActionListener(listener: OnEditorActionListener?) {
        debugRequire(actionListener == null)
        actionListener = listener
    }

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

    override fun beforeTextChanged(sequence: CharSequence, start: Int, count: Int, after: Int) = Unit
    override fun onTextChanged(sequence: CharSequence, start: Int, before: Int, count: Int) = Unit
    override fun afterTextChanged(editable: Editable) = Unit

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?/*indeed nullable*/): Boolean {
        actionListener?.onEditorAction(v, actionId, event)
        if (actionId == IME_ACTION_DONE) {
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
        if (!focused && ((imeOptions and IME_ACTION_DONE) == IME_ACTION_DONE)) {
            setText(submitted, BufferType.NORMAL)
        }
    }

    override fun performClick(): Boolean {
        requestFocus()
        return super.performClick()
    }

    override fun setBackground(background: Drawable?) {
        val drawable = filledDelegate?.makeBackgroundFilled(background) ?: background
        super.setBackground(drawable)
    }

    fun makeFilled(layout: TextInputLayout, @ColorInt filledColor: Int) {
        filledDelegate = FilledDelegate(
            textLayout = layout,
            textField = this,
            filledColor = filledColor,
            strokeWidth = layout.boxStrokeWidth,
            focusedStrokeColor = layout.boxStrokeColor,
            radius = resources.getDimension(R.dimen.corner_semi),
        )
    }

    interface OnSubmitListener {
        fun onCheck(value: String) = true
        fun onSubmit(value: String)
    }
}

fun TextInputLayout.showError(show: Boolean = true) = when {
    show -> showError(null)
    else -> showErrorIf(null)
}

fun TextInputLayout.showError(message: String? = null) {
    val err = message?.takeIf { it.isNotBlank() }
        ?: resources.getString(R.string.wrong_value)
    showErrorIf(err)
}

private fun TextInputLayout.showErrorIf(message: String?) {
    errorIconDrawable = null
    error = message
    isErrorEnabled = error != null
    isHelperTextEnabled = error != null
    if (error != null) getChildAt(1)
        ?.let { indicatorArea -> indicatorArea as? LinearLayout }
        ?.updatePaddingRelative(end = 0)
        .debugRequireNotNull()
}

private class FilledDelegate(
    private val textLayout: TextInputLayout,
    private val textField: EditText,
    private val filledColor: Int,
    private val strokeWidth: Int,
    private val focusedStrokeColor: Int,
    private val radius: Float,
) {

    init {
        textLayout.setBoxCornerRadii(radius, radius, radius, radius)
        textLayout.boxBackgroundMode = BOX_BACKGROUND_FILLED
        textLayout.boxStrokeWidth = 0
        textLayout.boxStrokeWidthFocused = 0
    }

    fun makeBackgroundFilled(background: Drawable?): Drawable? {
        debugRequire(background is MaterialShapeDrawable)
        background as MaterialShapeDrawable
        if (filledColor != Color.TRANSPARENT) background.fillColor = ColorStateList.valueOf(filledColor)
        background.strokeColor = ColorStateList.valueOf(Color.TRANSPARENT)
        background.strokeWidth = 0f
        if (!textField.isFocused) {
            return background
        }
        val strokeColor = textLayout.boxStrokeErrorColor
            ?.takeIf { textLayout.error != null }
            ?.defaultColor
            ?: focusedStrokeColor
        return HybridTextLayoutDrawable(background, strokeWidth, strokeColor, radius)
    }
}
