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
import app.atomofiron.searchboxapp.utils.preferences.PreferenceKeys

class TextFieldPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs)
    , Preference.SummaryProvider<TextFieldPreference>
    , TextField.Listener
{

    private val editText = TextField(context)
    private var filter: ((String) -> String?)? = null
    private var value = ""

    init {
        summaryProvider = this
    }

    override fun provideSummary(preference: TextFieldPreference): CharSequence? = value

    override fun onGetDefaultValue(array: TypedArray, index: Int): String? = array.getString(index)

    override fun onSetInitialValue(defaultValue: Any?) {
        value = getPersistedString((defaultValue as String?) ?: value)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        if (editText.parent == null) {
            val summary = holder.itemView.findViewById<View>(android.R.id.summary)
            addField(summary)
        }
        editText.setText(value)
    }

    public override fun onClick() {
        editText.isVisible = true
        editText.performClick()
    }

    private fun addField(summary: View) {
        val container = summary.parent as ViewGroup
        container.addView(editText, 1)
        editText.isGone = true
        editText.layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            addRule(RelativeLayout.BELOW, android.R.id.title)
        }
        editText.addListener(this)
        editText.setOnFocusChangeListener { _, hasFocus ->
            summary.alpha = if (hasFocus) Alpha.INVISIBLE else Alpha.VISIBLE
            editText.isGone = !hasFocus
        }
    }

    override fun onSubmit(value: String) {
        val value = PreferenceKeys.get<String>(key).check(value)
        val filtered = filter?.invoke(value) ?: value
        if (callChangeListener(filtered)) {
            persistString(filtered)
            if (filtered != this.value) {
                this.value = filtered
                notifyChanged()
            }
        }
    }
}