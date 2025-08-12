package app.atomofiron.searchboxapp.screens.explorer.fragment.sticky

import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.model.explorer.Node

data class HolderData(
    val item: Node,
    val holder: RecyclerView.ViewHolder,
) {
    val position get() = holder.bindingAdapterPosition
}