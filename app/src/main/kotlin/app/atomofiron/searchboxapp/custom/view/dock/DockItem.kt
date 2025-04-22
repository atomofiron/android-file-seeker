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
    val children: Children = Children.Stub,
) {
    companion object {
        val Stub = DockItem(0, 0, enabled = false)
    }
    val id = label.toLong()

    data class Children(
        private val items: List<DockItem> = emptyList(),
        val columns: Int,
    ) : List<DockItem> by items {
        companion object {
            val Stub = Children(columns = 0)
        }
        constructor(vararg items: DockItem, columns: Int = 1) : this(items.toList(), columns)
    }
}
