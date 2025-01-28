package app.atomofiron.searchboxapp.custom.view.menu.holder;

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener

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
        icon.isVisible = item.icon != null
        title.text = item.title
    }
}
