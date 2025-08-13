package app.atomofiron.searchboxapp.screens.explorer.fragment.sticky.info

import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.model.explorer.Node

data class HolderInfo(
    val item: Node,
    val holder: RecyclerView.ViewHolder,
) {
    val position get() = holder.bindingAdapterPosition
}