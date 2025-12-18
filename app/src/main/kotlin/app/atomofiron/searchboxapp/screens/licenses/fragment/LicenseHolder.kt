package app.atomofiron.searchboxapp.screens.licenses.fragment

import android.widget.TextView
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.screens.licenses.state.License

class LicenseHolder(private val textView: TextView) : GeneralHolder<License>(textView) {

    override fun onBind(item: License, position: Int) {
        textView.text = item.name
    }
}