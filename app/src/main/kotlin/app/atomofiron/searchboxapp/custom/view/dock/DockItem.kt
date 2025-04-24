package app.atomofiron.searchboxapp.custom.view.dock

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

sealed interface DockItem {
    val id: Long

    data class Button(
        @DrawableRes val icon: Int,
        @StringRes val label: Int,
        val enabled: Boolean,
        val selected: Boolean,
        val children: DockItemChildren,
    ) : DockItem {
        override val id = label.toLong()
    }

    data class Notch(
        val width: Int,
        val height: Int,
    ) : DockItem {
        override val id: Long = 1
        constructor(size: Int) : this(size, size)
    }

    companion object {
        val Stub = invoke(0, 0)

        operator fun invoke(
            @DrawableRes icon: Int,
            @StringRes label: Int,
            enabled: Boolean = true,
            selected: Boolean = false,
            children: DockItemChildren = DockItemChildren.Stub,
        ) = Button(icon, label, enabled, selected, children)
    }
}
