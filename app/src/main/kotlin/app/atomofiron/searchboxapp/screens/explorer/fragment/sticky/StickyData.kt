package app.atomofiron.searchboxapp.screens.explorer.fragment.sticky

import app.atomofiron.searchboxapp.custom.view.ExplorerStickyTopView
import app.atomofiron.searchboxapp.model.explorer.Node

data class StickyData(
    val position: Int,
    val item: Node,
    val view: ExplorerStickyTopView,
)
