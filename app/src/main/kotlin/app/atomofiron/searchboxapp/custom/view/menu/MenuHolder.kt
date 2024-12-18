package app.atomofiron.searchboxapp.custom.view.menu

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.dangerous.DangerousSliderView
import app.atomofiron.searchboxapp.utils.Const

sealed class MenuHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(item: MenuItem)
}

class MenuItemHolder private constructor(
    itemView: View,
    private val listener: MenuListener,
) : MenuHolder(itemView) {
    val icon: ImageView = itemView.findViewById(R.id.item_menu_iv_icon)
    val title: TextView = itemView.findViewById(R.id.item_menu_tv_label)

    private var itemId = 0

    constructor(parent: ViewGroup, listener: MenuListener) : this(
        LayoutInflater.from(parent.context).inflate(R.layout.item_curtain_menu, parent, false),
        listener,
    )

    init {
        itemView.setOnClickListener {
            listener.onMenuItemSelected(itemId)
        }
    }

    override fun bind(item: MenuItem) {
        itemId = item.itemId
        itemView.id = item.itemId
        icon.setImageDrawable(item.icon)
        title.text = item.title
    }
}

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
