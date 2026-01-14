package app.atomofiron.searchboxapp.model

import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemChildren
import app.atomofiron.searchboxapp.model.explorer.NodeSorting

fun NodeSorting.Companion.toDockItemChildren() = DockItemChildren(
    columns = 2,
    NodeSorting.Name.toDockItem(),
    NodeSorting.Name.Reversed.toDockItem(),
    NodeSorting.Date.toDockItem(),
    NodeSorting.Date.Reversed.toDockItem(),
    NodeSorting.Size.toDockItem(),
    NodeSorting.Size.Reversed.toDockItem(),
)

fun NodeSorting.toDockItem(): DockItem = toDockItem(this, null)

fun NodeSorting.toDockItem(id: DockItem.Id, label: DockItem.Label?): DockItem {
    return when (this) {
        NodeSorting.Name -> DockItem(id, DockItem.Icon(R.drawable.ic_sort_az_asc), label ?: DockItem.Label(R.string.sorting_a_z))
        NodeSorting.Name.Reversed -> DockItem(id, DockItem.Icon(R.drawable.ic_sort_az_desc), label ?: DockItem.Label(R.string.sorting_z_a))
        NodeSorting.Date -> DockItem(id, DockItem.Icon(R.drawable.ic_sort_time_asc), label ?: DockItem.Label(R.string.sorting_newest))
        NodeSorting.Date.Reversed -> DockItem(id, DockItem.Icon(R.drawable.ic_sort_time_desc), label ?: DockItem.Label(R.string.sorting_oldest))
        NodeSorting.Size -> DockItem(id, DockItem.Icon(R.drawable.ic_sort_weight_asc), label ?: DockItem.Label(R.string.sorting_smallest))
        NodeSorting.Size.Reversed -> DockItem(id, DockItem.Icon(R.drawable.ic_sort_weight_desc), label ?: DockItem.Label(R.string.sorting_largest))
    }
}
