package app.atomofiron.searchboxapp.screens.explorer.fragment.sticky.info

import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.model.explorer.Node

data class HolderInfo(
    val position: Int,
    val item: Node,
    val holder: RecyclerView.ViewHolder,
) {
    val view get() = holder.itemView
}