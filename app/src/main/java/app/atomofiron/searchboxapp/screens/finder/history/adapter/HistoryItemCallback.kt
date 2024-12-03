package app.atomofiron.searchboxapp.screens.finder.history.adapter

import androidx.recyclerview.widget.DiffUtil
import app.atomofiron.searchboxapp.screens.finder.history.dao.ItemHistory

object HistoryItemCallback : DiffUtil.ItemCallback<ItemHistory>() {
    override fun areItemsTheSame(oldItem: ItemHistory, newItem: ItemHistory): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: ItemHistory, newItem: ItemHistory): Boolean {
        return oldItem == newItem
    }
}