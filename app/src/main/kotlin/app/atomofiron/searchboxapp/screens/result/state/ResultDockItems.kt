package app.atomofiron.searchboxapp.screens.result.state

import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.toDockItemChildren

data class ResultDockState(
    val status: DockItem,
    val sorting: DockItem,
    val share: DockItem,
    val export: DockItem?,
    val confirm: DockItem?,
) : List<DockItem> by listOfNotNull(status, sorting, share, export, confirm) {
    companion object {
        val Default = ResultDockState(
            status = DockItem(
                DockItem.Id.Auto(),
                DockItem.Icon(R.drawable.ic_circle_check),
                DockItem.Label(R.string.completed),
                clickable = false,
            ),
            sorting = DockItem(
                DockItem.Id.Auto(),
                DockItem.Icon(R.drawable.ic_sort_az_desc),
                DockItem.Label(R.string.sorting),
                children = NodeSorting.toDockItemChildren(),
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
            confirm = DockItem(
                DockItem.Id.Auto(),
                DockItem.Icon(R.drawable.ic_circle_check),
                DockItem.Label(R.string.confirm),
                primary = true,
            ),
        )
        inline operator fun invoke(block: ResultDockState.() -> ResultDockState) = Default.run(block)
    }
}
