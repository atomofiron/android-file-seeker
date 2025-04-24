package app.atomofiron.searchboxapp.custom.preference

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.HapticFeedbackConstantsCompat.TOGGLE_OFF
import androidx.core.view.HapticFeedbackConstantsCompat.TOGGLE_ON
import androidx.preference.PreferenceViewHolder
import androidx.preference.R
import androidx.preference.SwitchPreferenceCompat

class SwitchPreference : SwitchPreferenceCompat, View.OnClickListener {

    private var newValue: Boolean? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView
            .findViewById<SwitchCompat>(R.id.switchWidget)
            .setOnClickListener(this)
    }

    override fun callChangeListener(newValue: Any?): Boolean {
        return super.callChangeListener(newValue)
            .also { if (it) this.newValue = newValue as Boolean? }
    }

    override fun onClick(view: View) {
        view as SwitchCompat
        if (view.isChecked == newValue) {
            newValue = null
            view.performHapticFeedback(if (view.isChecked) TOGGLE_ON else TOGGLE_OFF)
        }
    }
}