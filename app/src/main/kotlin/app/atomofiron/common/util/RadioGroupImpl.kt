package app.atomofiron.common.util

import android.widget.CompoundButton
import android.widget.RadioButton

class RadioGroupImpl : CompoundButton.OnCheckedChangeListener {
    private val buttons = ArrayList<RadioButton>()
    var checkedId = 0; private set

    var onCheckedChangeListener: (() -> Unit)? = null

    fun syncWith(button: RadioButton): RadioGroupImpl {
        require(!buttons.contains(button)) { Exception("RadioButton was already added!") }
        buttons.add(button)
        button.setOnCheckedChangeListener(this)
        if (button.isChecked) {
            checkedId = button.id
        }
        return this
    }

    fun clear(): RadioGroupImpl {
        checkedId = 0
        buttons.forEach { it.setOnCheckedChangeListener(null) }
        buttons.clear()
        return this
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (!isChecked) {
            return
        }
        checkedId = buttonView.id
        for (other in buttons) {
            if (other.id != buttonView.id) {
                other.isChecked = false
            }
        }
        onCheckedChangeListener?.invoke()
    }
}