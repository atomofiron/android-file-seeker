package app.atomofiron.searchboxapp.screens.explorer.fragment.sticky.info

import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.model.explorer.Node

data class HolderInfo(
    val position: Int,
    private val item: Node,
    val holder: RecyclerView.ViewHolder,
) {
    val uniqueId get() = item.uniqueId
    val ref get() = item.ref
    val view get() = holder.itemView
    val isOpened get() = item.isOpened
    val isSeparator get() = item.uniqueId == -ref.uniqueId

    fun areContentsTheSame(other: Node) = item.areContentsTheSame(other)
}