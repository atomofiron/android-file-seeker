package app.atomofiron.searchboxapp.screens.explorer.state

import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem

enum class ExplorerDock : DockItem.Id by DockItem.Id.Auto() {
    Search,
    Settings,
    Confirm,
}

data class ExplorerDockState(
    val search: DockItem,
    val settings: DockItem,
    val confirm: DockItem,
) : List<DockItem> by listOf(search, settings, confirm) {
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
            confirm = DockItem(
                ExplorerDock.Confirm,
                DockItem.Icon(R.drawable.ic_circle_check),
                DockItem.Label(R.string.confirm),
                primary = true,
            ),
        )

        operator fun <R> invoke(block: MutableList<DockItem>.(default: ExplorerDockState) -> R): List<DockItem> {
            return mutableListOf<DockItem>().apply { block(Default) }
        }
    }
}