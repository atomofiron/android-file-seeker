package app.atomofiron.searchboxapp.custom.view.dock.item

import androidx.recyclerview.widget.DiffUtil

class DockDiffCallback(
    private val old: List<DockItem>,
    private val new: List<DockItem>,
) : DiffUtil.Callback() {
    override fun getOldListSize() = old.size
    override fun getNewListSize() = new.size
    override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean = old[oldPosition].id == new[newPosition].id
    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean = old[oldPosition] == new[newPosition]
}
