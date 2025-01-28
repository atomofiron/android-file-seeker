package app.atomofiron.searchboxapp.custom.preference

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import androidx.annotation.StringRes
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.utils.getAttr

class ButtonPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleRes: Int = context.getAttr(
        androidx.preference.R.attr.preferenceStyle,
        android.R.attr.preferenceStyle,
    )
) : Preference(context, attrs, defStyleRes) {
    private var buttonText = ""
    private val buttonClickListener = ::onButtonClick

    init {
        makeSolid()
    }

    fun makeSolid() {
        widgetLayoutResource = R.layout.widget_button
        notifyChanged()
    }

    fun makeOutlined() {
        widgetLayoutResource = R.layout.widget_button_outlined
        notifyChanged()
    }

    fun setButtonText(@StringRes textId: Int) = setButtonText(context.getString(textId))

    fun setButtonText(text: String) {
        buttonText = text
        notifyChanged()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.findViewById<Button>(R.id.widgetButton)?.apply {
            text = buttonText
            visibility = if (buttonText.isEmpty()) View.GONE else View.VISIBLE
            setOnClickListener(buttonClickListener)
        }
    }

    private fun onButtonClick(v: View) {
        onPreferenceClickListener?.onPreferenceClick(this)
    }
}