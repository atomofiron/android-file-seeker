package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl.ExplorerItemBinderActionListener

interface ExplorerItemBinder {
    fun setOnItemActionListener(listener: ExplorerItemBinderActionListener?)
    fun bind(item: Node)
    fun bindComposition(composition: ExplorerItemComposition)
    fun disableClicks()
    fun hideCheckBox()
    fun showAlternatingBackground(visible: Boolean)
}