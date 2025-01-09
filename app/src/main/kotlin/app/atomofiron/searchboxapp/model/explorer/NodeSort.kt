package app.atomofiron.searchboxapp.model.explorer

sealed class NodeSort(val reversed: Boolean) {
    sealed class Name(reversed: Boolean) : NodeSort(reversed) {
        data object Reversed : Name(reversed = true)
        companion object : Name(reversed = false) {
            operator fun invoke(reversed: Boolean): NodeSort = if (reversed) Reversed else this
        }
    }
    sealed class Date(reversed: Boolean) : NodeSort(reversed) {
        data object Reversed : Date(reversed = true)
        companion object : Date(reversed = false) {
            operator fun invoke(reversed: Boolean): NodeSort = if (reversed) Reversed else this
        }
    }
    sealed class Size(reversed: Boolean) : NodeSort(reversed) {
        data object Reversed : Size(reversed = true)
        companion object : Size(reversed = false) {
            operator fun invoke(reversed: Boolean): NodeSort = if (reversed) Reversed else this
        }
    }
}