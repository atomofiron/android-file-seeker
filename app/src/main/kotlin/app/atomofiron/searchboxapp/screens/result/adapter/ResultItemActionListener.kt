package app.atomofiron.searchboxapp.screens.result.adapter

import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl

interface ResultItemActionListener : ExplorerItemBinderImpl.ExplorerItemBinderActionListener {
    fun onItemVisible(item: ResultItem.Item)
}