package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

class TargetsHolder(parent: ViewGroup, layoutId: Int) : GeneralHolder<FinderStateItem>(parent, layoutId) {

    private val recyclerView = itemView.findViewById<RecyclerView>(R.id.item_rv_targets)
    private val adapter = TargetAdapter()

    init {
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.TargetsItem
        adapter.submitList(item.targets)
    }

    private class TargetAdapter : ListAdapter<Node, TargetHolder>(DiffUtilCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TargetHolder(parent, R.layout.item_finder_target)

        override fun onBindViewHolder(holder: TargetHolder, position: Int) {
            holder.bind(currentList[position], position)
        }
    }

    private class DiffUtilCallback : DiffUtil.ItemCallback<Node>() {
        override fun areItemsTheSame(oldItem: Node, newItem: Node): Boolean = oldItem.uniqueId == newItem.uniqueId

        // NodeContent is a sealed class
        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Node, newItem: Node): Boolean {
            return oldItem.content == newItem.content && oldItem.children?.size == newItem.children?.size
        }
    }
}