package app.atomofiron.searchboxapp.screens.finder.model

import android.graphics.drawable.Drawable
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.DockItem

enum class FinderDock(override val value: Long) : DockItem.Id {
    Files(0), Settings(1)
}

fun finderDockItems(settings: Drawable) = listOf(
    DockItem(
        FinderDock.Files,
        R.drawable.ic_tree,
        R.string.search,
    ),
    DockItem(
        FinderDock.Settings,
        settings,
        R.string.settings,
    ),
)