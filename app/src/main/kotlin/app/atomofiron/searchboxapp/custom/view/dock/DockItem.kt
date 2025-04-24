package app.atomofiron.searchboxapp.custom.view.dock

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class DockItem(
    @DrawableRes
    val icon: Int,
    @StringRes
    val label: Int,
    val enabled: Boolean = true,
    val selected: Boolean = false,
    val children: DockItemChildren = DockItemChildren.Stub,
) {
    companion object {
        val Notch = DockItem(0, 0, enabled = false)
    }
    val id = label.toLong()
}
