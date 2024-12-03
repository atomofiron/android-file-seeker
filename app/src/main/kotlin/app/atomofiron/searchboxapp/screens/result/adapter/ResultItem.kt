package app.atomofiron.searchboxapp.screens.result.adapter

import app.atomofiron.searchboxapp.model.finder.ItemMatch

sealed class ResultItem(val uniqueId: Int) {
    data class Header(val dirsCount: Int, val filesCount: Int) : ResultItem(1)
    data class Item(val item: ItemMatch) : ResultItem(item.count)
}