package app.atomofiron.searchboxapp.screens.result.adapter

import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinder

interface ResultItemActionListener : ExplorerItemBinder.ExplorerItemBinderActionListener {
    fun onItemVisible(item: ResultItem.Item)
    fun onErrorsClick()
}