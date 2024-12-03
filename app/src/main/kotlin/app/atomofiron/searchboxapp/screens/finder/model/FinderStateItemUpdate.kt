package app.atomofiron.searchboxapp.screens.finder.model

sealed class FinderStateItemUpdate(val index: Int) {
    class Changed(index: Int, val item: FinderStateItem) : FinderStateItemUpdate(index)
    class Inserted(index: Int, val item: FinderStateItem) : FinderStateItemUpdate(index)
    class Removed(index: Int) : FinderStateItemUpdate(index)
}