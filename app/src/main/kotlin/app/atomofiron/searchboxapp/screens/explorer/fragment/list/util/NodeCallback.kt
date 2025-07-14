package app.atomofiron.searchboxapp.screens.explorer.fragment.list.util

import androidx.recyclerview.widget.DiffUtil
import app.atomofiron.searchboxapp.model.explorer.Node

object NodeCallback : DiffUtil.ItemCallback<Node>() {

    override fun areItemsTheSame(oldItem: Node, newItem: Node): Boolean {
        return newItem.uniqueId == oldItem.uniqueId && newItem.isDeepest == oldItem.isDeepest
    }

    override fun areContentsTheSame(oldItem: Node, newItem: Node): Boolean = newItem.areContentsTheSame(oldItem)
}