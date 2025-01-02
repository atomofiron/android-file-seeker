package app.atomofiron.searchboxapp.custom.view.menu.holder

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.dangerous.DangerousSliderView
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.searchboxapp.utils.Const

class DangerousMenuItemHolder private constructor(
    val view: DangerousSliderView,
    private val listener: MenuListener,
) : MenuHolder(view) {

    private var itemId = 0

    constructor(parent: ViewGroup, listener: MenuListener) : this(
        LayoutInflater.from(parent.context).inflate(R.layout.item_curtain_menu_dangerous, parent, false) as DangerousSliderView,
        listener,
    )

    init {
        view.listener = {
            listener.onMenuItemSelected(itemId)
            true
        }
    }

    override fun bind(item: MenuItem) {
        itemId = item.itemId
        view.setText(item.title)
        view.resources
            .getString(R.string.slide_to)
            .replace(Const.PLACEHOLDER, item.title.toString().lowercase())
            .let { view.setTip(it) }
    }
}
