package app.atomofiron.searchboxapp.screens.result.state

import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemChildren
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.toDockItem

data class ResultDockState(
    val status: DockItem,
    val sorting: DockItem,
    val share: DockItem,
    val export: DockItem,
) : List<DockItem> by listOf(status, sorting, share, export) {
    companion object {
        val Default = ResultDockState(
            status = DockItem(
                DockItem.Id.Auto(),
                DockItem.Icon(R.drawable.ic_circle_stop),
                DockItem.Label.Empty,
                clickable = false,
            ),
            sorting = DockItem(
                DockItem.Id.Auto(),
                DockItem.Icon(R.drawable.ic_sort_az_desc),
                DockItem.Label(R.string.sorting),
                children = DockItemChildren(
                    columns = 2,
                    NodeSorting.Name.toDockItem(),
                    NodeSorting.Name.Reversed.toDockItem(),
                    NodeSorting.Date.toDockItem(),
                    NodeSorting.Date.Reversed.toDockItem(),
                    NodeSorting.Size.toDockItem(),
                    NodeSorting.Size.Reversed.toDockItem(),
                )
            ),
            share = DockItem(
                DockItem.Id.Auto(),
                DockItem.Icon(R.drawable.ic_share),
                DockItem.Label(R.string.share),
                enabled = false,
            ),
            export = DockItem(
                DockItem.Id.Auto(),
                DockItem.Icon(R.drawable.ic_document_send),
                DockItem.Label(R.string.export_btn),
                enabled = false,
            ),
        )
        inline operator fun invoke(block: ResultDockState.() -> ResultDockState) = Default.run(block)
    }
}
