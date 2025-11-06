package app.atomofiron.searchboxapp.screens.result.adapter

import app.atomofiron.searchboxapp.model.finder.ItemMatch

sealed class ResultItem(
    val uniqueId: Int,
    val viewType: Int,
) {
    data class Header(
        val dirCount: Int,
        val fileCount: Int,
        val errorCount: Int,
    ) : ResultItem(1, ResultViewType.Header.viewType)

    data class Item(val match: ItemMatch) : ResultItem(match.count, ResultViewType.Item.viewType)
}