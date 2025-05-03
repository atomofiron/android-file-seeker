package app.atomofiron.searchboxapp.screens.explorer.state

import android.graphics.drawable.Drawable
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemChildren
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.toDockItem

enum class ExplorerDock(override val value: Long) : DockItem.Id {
    Search(0), Sorting(1), Delete(2), Settings(3)
}

fun explorerDockItems(settings: Drawable) = listOfNotNull(
    DockItem(
        ExplorerDock.Search,
        DockItem.Icon(R.drawable.ic_search),
        DockItem.Label(R.string.search),
    ),
    DockItem(
        ExplorerDock.Sorting,
        DockItem.Icon(R.drawable.ic_sort_az_asc),
        DockItem.Label(R.string.sorting),
        children = DockItemChildren(
            columns = 2,
            NodeSorting.Name.toDockItem().copy(selected = true),
            NodeSorting.Name.Reversed.toDockItem(),
            NodeSorting.Date.toDockItem(),
            NodeSorting.Date.Reversed.toDockItem(),
            NodeSorting.Size.toDockItem(),
            NodeSorting.Size.Reversed.toDockItem(),
        )
    ).takeIf { BuildConfig.DEBUG },
    DockItem(
        ExplorerDock.Delete,
        DockItem.Icon(R.drawable.ic_trashbox),
        DockItem.Label(R.string.delete),
        children = DockItemChildren(
            NodeSorting.Name.toDockItem().copy(selected = true),
            NodeSorting.Name.Reversed.toDockItem(),
            NodeSorting.Date.toDockItem(),
            NodeSorting.Date.Reversed.toDockItem(),
            NodeSorting.Size.toDockItem(),
            NodeSorting.Size.Reversed.toDockItem(),
        )
    ).takeIf { BuildConfig.DEBUG },
    DockItem(
        ExplorerDock.Settings,
        DockItem.Icon(settings),
        DockItem.Label(R.string.settings),
    ),
)