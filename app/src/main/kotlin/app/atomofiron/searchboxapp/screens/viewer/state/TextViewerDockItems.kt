package app.atomofiron.searchboxapp.screens.viewer.state

import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.utils.Id

data class TextViewerDockState(
    val status: DockItem,
    val search: DockItem,
    val previous: DockItem,
    val next: DockItem,
) : List<DockItem> by listOf(status, search, previous, next) {
    companion object {
        val Default = TextViewerDockState(
            status = DockItem(
                Id(0),
                DockItem.Icon(R.drawable.ic_circle_check),
                DockItem.Label(R.string.status),
                clickable = false,
            ),
            search = DockItem(
                Id(1),
                DockItem.Icon(R.drawable.ic_search),
                DockItem.Label(R.string.search),
            ),
            previous = DockItem(
                Id(2),
                DockItem.Icon(R.drawable.ic_previous),
                DockItem.Label(R.string.previous),
                enabled = false,
            ),
            next = DockItem(
                Id(3),
                DockItem.Icon(R.drawable.ic_next),
                DockItem.Label(R.string.next),
                enabled = false,
            )
        )

        inline operator fun invoke(block: TextViewerDockState.() -> Unit) = Default.run(block)
    }
}
