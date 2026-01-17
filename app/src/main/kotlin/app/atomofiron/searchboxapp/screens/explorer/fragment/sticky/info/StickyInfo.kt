package app.atomofiron.searchboxapp.screens.explorer.fragment.sticky.info

import android.view.View
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRef

data class StickyInfo<V : View>(
    val position: Int,
    private val item: Node,
    val view: V,
    val sortedChildren: List<NodeRef>,
) {
    val uniqueId get() = item.uniqueId
    val ref get() = item.ref
    val isOpened get() = item.isOpened
    val isDeepest get() = item.isDeepest

    fun getOpenedId() = item.getOpenedId()
    fun areContentsTheSame(other: Node) = item.areContentsTheSame(other)
}
