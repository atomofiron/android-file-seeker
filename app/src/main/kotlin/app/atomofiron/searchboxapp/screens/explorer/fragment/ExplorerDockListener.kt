package app.atomofiron.searchboxapp.screens.explorer.fragment

import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem

interface ExplorerDockListener {
    fun onSearchClick()
    fun onSortPick(item: DockItem)
    fun onCopyClick()
    fun onPasteClick(move: Boolean)
    fun onSettingsClick()
    fun onConfirmClick()
}