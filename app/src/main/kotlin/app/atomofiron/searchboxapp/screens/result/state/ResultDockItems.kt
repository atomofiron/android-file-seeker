package app.atomofiron.searchboxapp.screens.result.state

import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemChildren

data class ResultDockState(
    val status: DockItem,
    val sorting: DockItem,
    val share: DockItem,
    val export: DockItem,
) : List<DockItem> by listOf(status, sorting, share, export) {
    companion object {
        val Default = ResultDockState(
            status = DockItem(
                DockItem.Id(0),
                DockItem.Icon(R.drawable.ic_circle_stop),
                DockItem.Label(""),
                clickable = false,
            ),
            sorting = DockItem(
                DockItem.Id(1),
                DockItem.Icon(R.drawable.ic_sort_az_asc),
                DockItem.Label(R.string.sorting),
                children = DockItemChildren(
                    columns = 2,
                    DockItem(DockItem.Id(10), DockItem.Icon(R.drawable.ic_sort_az_asc), DockItem.Label(R.string.from_start), selected = true),
                    DockItem(DockItem.Id(11), DockItem.Icon(R.drawable.ic_sort_az_desc), DockItem.Label(R.string.from_end)),
                    DockItem(DockItem.Id(12), DockItem.Icon(R.drawable.ic_sort_time_asc), DockItem.Label(R.string.new_ones)),
                    DockItem(DockItem.Id(13), DockItem.Icon(R.drawable.ic_sort_time_desc), DockItem.Label(R.string.old_ones)),
                    DockItem(DockItem.Id(14), DockItem.Icon(R.drawable.ic_sort_weight_asc), DockItem.Label(R.string.lite)),
                    DockItem(DockItem.Id(15), DockItem.Icon(R.drawable.ic_sort_weight_desc), DockItem.Label(R.string.heavy)),
                )
            ),
            share = DockItem(
                DockItem.Id(2),
                DockItem.Icon(R.drawable.ic_share),
                DockItem.Label(R.string.share),
                enabled = false,
            ),
            export = DockItem(
                DockItem.Id(3),
                DockItem.Icon(R.drawable.ic_document_send),
                DockItem.Label(R.string.export_btn),
                enabled = false,
            ),
        )
        inline operator fun invoke(block: ResultDockState.() -> ResultDockState) = Default.run(block)
    }
}
