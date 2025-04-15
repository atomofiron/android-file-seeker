package app.atomofiron.searchboxapp.screens.result.adapter

import androidx.recyclerview.widget.DiffUtil

object ResultDiffUtilCallback : DiffUtil.ItemCallback<ResultItem>() {

    override fun areItemsTheSame(oldItem: ResultItem, newItem: ResultItem): Boolean = oldItem.uniqueId == newItem.uniqueId

    override fun areContentsTheSame(oldItem: ResultItem, newItem: ResultItem): Boolean = oldItem == newItem
}