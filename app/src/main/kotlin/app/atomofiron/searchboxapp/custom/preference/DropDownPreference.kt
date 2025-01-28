package app.atomofiron.searchboxapp.custom.preference

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ArrayAdapter
import androidx.preference.DropDownPreference
import app.atomofiron.fileseeker.R

@SuppressLint("PrivateResource")
class DropDownPreference(
    context: Context,
    attrs: AttributeSet? = null,
) : DropDownPreference(context, attrs) {

    init {
        layoutResource = androidx.preference.R.layout.preference_material
        widgetLayoutResource = R.layout.widget_spinner
        super.setSummary(null)
    }

    override fun createAdapter(): ArrayAdapter<*> = ArrayAdapter<Any?>(context, R.layout.item_drop_down)

    override fun setSummary(summary: CharSequence?) = super.setSummary(null)
}