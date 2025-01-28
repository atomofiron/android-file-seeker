package app.atomofiron.searchboxapp.custom.preference

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.ByteSizeTextField

class ByteSizePreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
    private var editText: ByteSizeTextField? = null
    private var value = 0

    init {
        widgetLayoutResource = R.layout.preference_byte_size
    }

    override fun onGetDefaultValue(array: TypedArray, index: Int): Int = array.getInt(index, 0)

    override fun onSetInitialValue(defaultValue: Any?) {
        value = getPersistedInt((defaultValue as? Int) ?: value)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        if (editText == null) {
            val editText = holder.itemView.findViewById<ByteSizeTextField>(R.id.size)
            editText.setOnSubmitListener(::onSubmit)
            editText.setValue(value)
            this.editText = editText
        }
    }

    override fun onDetached() {
        super.onDetached()
        editText = null
    }

    public override fun onClick() {
        editText?.performClick()
    }

    private fun onSubmit(value: Int) {
        if (callChangeListener(value)) {
            persistInt(value)
            this.value = value
        }
    }
}