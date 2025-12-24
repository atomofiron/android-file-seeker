package app.atomofiron.searchboxapp.screens.result.adapter

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeId
import app.atomofiron.searchboxapp.model.finder.ItemMatch

sealed class ResultItem(
    val uniqueId: NodeId,
    val viewType: Int,
) {
    data class Header(
        val dirCount: Int,
        val fileCount: Int,
        val errorCount: Int,
    ) : ResultItem(1, ResultViewType.Header.viewType)

    data class Item(
        val match: ItemMatch,
        val item: Node,
    ) : ResultItem(match.uniqueId, ResultViewType.Item.viewType) {
        val ref get() = item.ref
        val isDirectory get() = item.isDirectory
        val isChecked get() = item.isChecked
        val isCached get() = item.isCached
    }
}