package app.atomofiron.searchboxapp.model

import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.model.explorer.NodeSorting

fun NodeSorting.toDockItem(label: DockItem.Label? = null): DockItem {
    return when (this) {
        NodeSorting.Name -> DockItem(this, DockItem.Icon(R.drawable.ic_sort_az_asc), label ?: DockItem.Label(R.string.sorting_a_z))
        NodeSorting.Name.Reversed -> DockItem(this, DockItem.Icon(R.drawable.ic_sort_az_desc), label ?: DockItem.Label(R.string.sorting_z_a))
        NodeSorting.Date -> DockItem(this, DockItem.Icon(R.drawable.ic_sort_time_asc), label ?: DockItem.Label(R.string.sorting_oldest))
        NodeSorting.Date.Reversed -> DockItem(this, DockItem.Icon(R.drawable.ic_sort_time_desc), label ?: DockItem.Label(R.string.sorting_newest))
        NodeSorting.Size -> DockItem(this, DockItem.Icon(R.drawable.ic_sort_weight_asc), label ?: DockItem.Label(R.string.sorting_smallest))
        NodeSorting.Size.Reversed -> DockItem(this, DockItem.Icon(R.drawable.ic_sort_weight_desc), label ?: DockItem.Label(R.string.sorting_largest))
    }
}
