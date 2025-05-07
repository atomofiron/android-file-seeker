package app.atomofiron.searchboxapp.screens.explorer.state

import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem

enum class ExplorerDock(override val value: Long) : DockItem.Id {
    Search(0), Settings(1)
}

data class ExplorerDockState(
    val search: DockItem,
    val settings: DockItem,
) : List<DockItem> by listOf(search, settings) {
    companion object {
        val Default = ExplorerDockState(
            search = DockItem(
                ExplorerDock.Search,
                DockItem.Icon(R.drawable.ic_search),
                DockItem.Label(R.string.search),
            ),
            settings = DockItem(
                ExplorerDock.Settings,
                DockItem.Icon(R.drawable.ic_settings),
                DockItem.Label(R.string.settings),
            ),
        )
    }
}