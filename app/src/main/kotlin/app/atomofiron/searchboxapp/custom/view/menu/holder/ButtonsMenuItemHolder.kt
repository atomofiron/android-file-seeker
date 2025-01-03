package app.atomofiron.searchboxapp.custom.view.menu.holder;

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.SubMenu
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.size
import androidx.core.view.updateLayoutParams
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener

class ButtonsMenuItemHolder private constructor(
    private val layout: LinearLayout,
    private val listener: MenuListener,
) : MenuHolder(layout) {

    constructor(parent: ViewGroup, listener: MenuListener) : this(
        LayoutInflater.from(parent.context).inflate(R.layout.item_curtain_menu_buttons, parent, false) as LinearLayout,
        listener,
    )

    override fun bind(item: MenuItem) {
        itemView.id = item.itemId
        val subMenu = item.subMenu ?: return
        subMenu.completeButtons()
        subMenu.bindButtons()
    }

    private fun SubMenu.completeButtons() {
        while (true) when {
            layout.childCount > size() -> layout.removeViewAt(0)
            layout.childCount < size() -> LayoutInflater.from(layout.context)
                .inflate(R.layout.button_tonal, layout)
            else -> break
        }
    }

    private fun SubMenu.bindButtons() {
        for (i in 0..<size) {
            layout.getChildAt(i)
                .let { it as Button }
                .bind(getItem(i))
        }
    }

    private fun Button.bind(item: MenuItem) {
        id = item.itemId
        text = item.title
        compoundDrawableTintList = textColors
        setCompoundDrawablesRelativeWithIntrinsicBounds(item.icon, null, null, null)
        updateLayoutParams<LinearLayout.LayoutParams> {
            width = 0
            weight = 1f
        }
        setOnClickListener {
            listener.onMenuItemSelected(item.itemId)
        }
    }
}
