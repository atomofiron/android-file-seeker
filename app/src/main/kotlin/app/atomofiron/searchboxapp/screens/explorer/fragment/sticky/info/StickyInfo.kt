package app.atomofiron.searchboxapp.screens.explorer.fragment.sticky.info

import android.view.View
import app.atomofiron.searchboxapp.model.explorer.Node

data class StickyInfo<V : View>(
    val position: Int,
    val item: Node,
    val view: V,
)
