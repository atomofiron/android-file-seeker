package app.atomofiron.searchboxapp.screens.explorer.fragment

import android.graphics.drawable.Drawable
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.DockItem

enum class ExplorerDock(override val value: Long) : DockItem.Id {
    Search(0), Settings(1)
}

fun explorerDockItems(settings: Drawable) = listOf(
    DockItem(
        ExplorerDock.Search,
        R.drawable.ic_search,
        R.string.search,
    ),
    DockItem(
        ExplorerDock.Settings,
        settings,
        R.string.settings,
    ),
)