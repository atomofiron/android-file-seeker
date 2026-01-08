package app.atomofiron.searchboxapp.screens.explorer.state

import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.toDockItemChildren

object ExplorerDock : DockItem.Id.Factory {
    val Search = DockItem(
        nextId(),
        DockItem.Icon(R.drawable.ic_search),
        DockItem.Label(R.string.search),
    )
    val Sorting = DockItem(
        nextId(),
        DockItem.Icon(R.drawable.ic_sort),
        DockItem.Label(R.string.sorting),
        children = NodeSorting.toDockItemChildren(),
    )
    val PasteCopy = DockItem(
        nextId(),
        DockItem.Icon(R.drawable.ic_stub),
        DockItem.Label(R.string.by_copying),
    )
    val PasteMove = DockItem(
        nextId(),
        DockItem.Icon(R.drawable.ic_stub),
        DockItem.Label(R.string.by_moving),
    )
    val Copy = DockItem(
        nextId(),
        DockItem.Icon(R.drawable.ic_stub),
        DockItem.Label(R.string.copy),
    )
    val Paste = DockItem(
        nextId(),
        DockItem.Icon(R.drawable.ic_stub),
        DockItem.Label(R.string.paste),
    )
    val Settings = DockItem(
        nextId(),
        DockItem.Icon(R.drawable.ic_settings),
        DockItem.Label(R.string.settings),
    )
    val Confirm = DockItem(
        nextId(),
        DockItem.Icon(R.drawable.ic_circle_check),
        DockItem.Label(R.string.confirm),
        primary = true,
    )

    operator fun <R> invoke(block: MutableList<DockItem>.(ExplorerDock) -> R): List<DockItem> {
        return mutableListOf<DockItem>().apply { block(ExplorerDock) }
    }
}