package app.atomofiron.searchboxapp.model.other

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition

data class ExplorerItemOptions constructor(
    val ids: List<Int>,
    val items: List<Node>,
    val composition: ExplorerItemComposition,
    val checked: List<Int> = emptyList(),
)