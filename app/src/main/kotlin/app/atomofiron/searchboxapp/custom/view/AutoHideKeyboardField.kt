package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import app.atomofiron.fileseeker.R
import com.google.android.material.textfield.TextInputEditText
import androidx.core.content.withStyledAttributes

open class AutoHideKeyboardField : TextInputEditText {

    private val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    protected var hideKeyboardOnFocusLost: Boolean = true
    protected var hideKeyboardOnDetached: Boolean = true

    private var listeners = mutableListOf<OnFocusChangeListener>()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        attrs?.obtain()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        attrs?.obtain(defStyleAttr)
    }

    init {
        imeOptions = imeOptions or EditorInfo.IME_FLAG_NO_EXTRACT_UI or EditorInfo.IME_FLAG_NO_FULLSCREEN
    }

    private fun AttributeSet.obtain(defStyleAttr: Int = 0) {
        context.withStyledAttributes(this, R.styleable.AutoHideKeyboardField, defStyleAttr, 0) {
            hideKeyboardOnFocusLost = getBoolean(R.styleable.AutoHideKeyboardField_hideKeyboardOnFocusLost, hideKeyboardOnFocusLost)
        }
    }

    fun hideKeyboardOnFocusLost(value: Boolean = true) {
        hideKeyboardOnFocusLost = value
    }

    fun addOnFocusChangeListener(listener: OnFocusChangeListener): Boolean {
        return listeners.all { it !== listener }
            .also { if (it) listeners.add(listener) }
    }

    fun removeOnFocusChangeListener(listener: OnFocusChangeListener): Boolean = listeners.remove(listener)

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        val result = super.onKeyPreIme(keyCode, event)
        if ((keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) && hasFocus()) {
            clearFocus()
            inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
            // перехватываем событие, чтобы оно не обработалось при уже закрытой клавиатуре
            return true
        }
        return result
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        val keyboard = resources.configuration.keyboard
        when {
            !hideKeyboardOnFocusLost -> Unit
            keyboard == Configuration.KEYBOARD_QWERTY -> Unit
            !focused -> inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
            else -> {
                inputMethodManager.showSoftInput(this, 0)
                setSelection(length())
            }
        }
        listeners.forEach { it.onFocusChange(this, focused) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (hideKeyboardOnDetached) {
            inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
        }
    }
}