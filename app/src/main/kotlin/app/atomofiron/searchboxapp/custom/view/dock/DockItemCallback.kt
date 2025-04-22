package app.atomofiron.searchboxapp.custom.view.dock

import androidx.recyclerview.widget.DiffUtil

class DockItemCallback : DiffUtil.ItemCallback<DockItem>() {
    override fun areItemsTheSame(oldItem: DockItem, newItem: DockItem) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: DockItem, newItem: DockItem) = oldItem == newItem
}
