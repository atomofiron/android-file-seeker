package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import kotlinx.serialization.Serializable

@Serializable
sealed class NodeSorting(val reversed: Boolean) : DockItem.Id.Auto() {
    sealed class Name(reversed: Boolean) : NodeSorting(reversed) {
        data object Reversed : Name(reversed = true) {
            override fun toString() = "Name.Reversed"
        }
        companion object : Name(reversed = false) {
            operator fun invoke(reversed: Boolean): NodeSorting = if (reversed) Reversed else this
            override fun toString() = "Name"
        }
    }
    sealed class Date(reversed: Boolean) : NodeSorting(reversed) {
        data object Reversed : Date(reversed = true) {
            override fun toString() = "Date.Reversed"
        }
        companion object : Date(reversed = false) {
            operator fun invoke(reversed: Boolean): NodeSorting = if (reversed) Reversed else this
            override fun toString() = "Date"
        }
    }
    sealed class Size(reversed: Boolean) : NodeSorting(reversed) {
        data object Reversed : Size(reversed = true) {
            override fun toString() = "Size.Reversed"
        }
        companion object : Size(reversed = false) {
            operator fun invoke(reversed: Boolean): NodeSorting = if (reversed) Reversed else this
            override fun toString() = "Size"
        }
    }
}