package app.atomofiron.searchboxapp.screens.explorer.state

import android.graphics.drawable.Drawable
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.DockItem

enum class ExplorerDock(override val value: Long) : DockItem.Id {
    Search(0), Settings(1)
}

fun explorerDockItems(settings: Drawable) = listOf(
    DockItem(
        ExplorerDock.Search,
        DockItem.Icon(R.drawable.ic_search),
        DockItem.Label(R.string.search),
    ),
    DockItem(
        ExplorerDock.Settings,
        DockItem.Icon(settings),
        DockItem.Label(R.string.settings),
        enabled = false,
    ),
)