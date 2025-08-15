package app.atomofiron.searchboxapp.model.other

import app.atomofiron.searchboxapp.custom.view.menu.MenuItem
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition

data class ExplorerItemOptions(
    val operations: List<MenuItem>,
    val items: List<Node>,
    val composition: ExplorerItemComposition,
) : List<MenuItem> by operations