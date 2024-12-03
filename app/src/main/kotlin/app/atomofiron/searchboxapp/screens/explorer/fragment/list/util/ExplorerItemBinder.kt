package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerItemActionListener

interface ExplorerItemBinder {
    fun setOnItemActionListener(listener: ExplorerItemActionListener?)
    fun onBind(item: Node)
    fun bindComposition(composition: ExplorerItemComposition, preview: Boolean = false)
    fun disableClicks()
    fun hideCheckBox()
    fun setGreyBackgroundColor(visible: Boolean = true)
}