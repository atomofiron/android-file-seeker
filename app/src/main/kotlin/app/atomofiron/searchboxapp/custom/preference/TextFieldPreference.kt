package app.atomofiron.searchboxapp.custom.preference

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.RelativeLayout.LayoutParams
import android.widget.RelativeLayout.LayoutParams.MATCH_PARENT
import android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import app.atomofiron.searchboxapp.custom.view.TextField
import app.atomofiron.searchboxapp.utils.Alpha

class TextFieldPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
    private lateinit var editText: TextField
    private lateinit var summary: View
    private var value = ""
    private var filter: ((String) -> String?)? = null

    fun setFilter(filter: (String) -> String?) {
        this.filter = filter
    }

    override fun onGetDefaultValue(array: TypedArray, index: Int): String? = array.getString(index)

    override fun onSetInitialValue(defaultValue: Any?) {
        value = (defaultValue as? String) ?: getPersistedString(value)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        if (editText.parent == null) {
            summary = holder.itemView.findViewById(android.R.id.summary)
            editText.layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                addRule(RelativeLayout.BELOW, android.R.id.title)
            }
            editText.setText(value)
            (summary.parent as ViewGroup).addView(editText, 1)
        }
    }

    override fun onAttached() {
        super.onAttached()
        initField(context)
    }

    public override fun onClick() {
        summary.alpha = Alpha.INVISIBLE
        editText.isVisible = true
        editText.performClick()
    }

    private fun initField(context: Context) {
        editText = TextField(context)
        editText.isGone = true
        editText.setOnSubmitListener(::onSubmit)
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                summary.alpha = Alpha.VISIBLE
                editText.isGone = true
            }
        }
    }

    private fun onSubmit(value: String) {
        val filtered = filter?.invoke(value) ?: value
        if (callChangeListener(filtered)) {
            persistString(filtered)
            this.value = value
        }
    }
}