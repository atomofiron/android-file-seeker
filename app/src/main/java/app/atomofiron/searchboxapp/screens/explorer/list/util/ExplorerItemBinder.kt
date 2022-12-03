package app.atomofiron.searchboxapp.screens.explorer.list.util

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.list.ExplorerItemActionListener

interface ExplorerItemBinder {
    fun setOnItemActionListener(listener: ExplorerItemActionListener?)
    fun onBind(item: Node)
    fun bindComposition(composition: ExplorerItemComposition)
    fun disableClicks()
    fun hideCheckBox()
    fun setGreyBackgroundColor(visible: Boolean = true)
}