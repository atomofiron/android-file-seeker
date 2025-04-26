package app.atomofiron.searchboxapp.screens.finder.state

import android.graphics.drawable.Drawable
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem

enum class FinderDock(override val value: Long) : DockItem.Id {
    Files(0), Settings(1)
}

fun finderDockItems(settings: Drawable) = listOf(
    DockItem(
        FinderDock.Files,
        DockItem.Icon(R.drawable.ic_tree),
        DockItem.Label(R.string.search),
    ),
    DockItem(
        FinderDock.Settings,
        DockItem.Icon(settings),
        DockItem.Label(R.string.settings),
    ),
)