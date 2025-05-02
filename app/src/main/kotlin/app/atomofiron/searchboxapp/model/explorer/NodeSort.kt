package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem

sealed class NodeSorting(val reversed: Boolean) : DockItem.Id.Auto() {
    sealed class Name(reversed: Boolean) : NodeSorting(reversed) {
        data object Reversed : Name(reversed = true)
        companion object : Name(reversed = false) {
            operator fun invoke(reversed: Boolean): NodeSorting = if (reversed) Reversed else this
        }
    }
    sealed class Date(reversed: Boolean) : NodeSorting(reversed) {
        data object Reversed : Date(reversed = true)
        companion object : Date(reversed = false) {
            operator fun invoke(reversed: Boolean): NodeSorting = if (reversed) Reversed else this
        }
    }
    sealed class Size(reversed: Boolean) : NodeSorting(reversed) {
        data object Reversed : Size(reversed = true)
        companion object : Size(reversed = false) {
            operator fun invoke(reversed: Boolean): NodeSorting = if (reversed) Reversed else this
        }
    }
}