package app.atomofiron.searchboxapp.custom.view.menu.holder;

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.SubMenu
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.view.size
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.menu.MenuListener
import app.atomofiron.fileseeker.databinding.ButtonWithCrossBinding

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
                .inflate(R.layout.button_with_cross, layout)
            else -> break
        }
    }

    private fun SubMenu.bindButtons() {
        for (i in 0..<size) {
            layout.getChildAt(i)
                .let { ButtonWithCrossBinding.bind(it) }
                .bind(getItem(i))
        }
    }

    private fun ButtonWithCrossBinding.bind(item: MenuItem) {
        val isChecked = item.isChecked && item.isEnabled
        common.isVisible = isChecked
        tonal.isVisible = !isChecked
        tonal.isEnabled = item.isEnabled
        if (isChecked) common.bind(item) else tonal.bind(item)
        cross.clipToOutline = true
        cross.isVisible = isChecked
        if (isChecked) cross.setOnClickListener {
            listener.onMenuItemSelected(-itemView.id)
            item.isChecked = false
            bind(item)
        }
    }

    private fun Button.bind(item: MenuItem) {
        isVisible = true
        id = item.itemId
        text = item.title
        compoundDrawableTintList = textColors
        setCompoundDrawablesRelativeWithIntrinsicBounds(item.icon, null, null, null)
        setOnClickListener {
            listener.onMenuItemSelected(item.itemId)
        }
    }
}
