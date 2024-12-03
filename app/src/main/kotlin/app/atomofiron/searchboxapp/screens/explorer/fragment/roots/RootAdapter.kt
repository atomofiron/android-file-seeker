package app.atomofiron.searchboxapp.screens.explorer.fragment.roots

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.explorer.NodeRoot

private class ItemCallbackImpl : DiffUtil.ItemCallback<NodeRoot>() {
    override fun areItemsTheSame(oldItem: NodeRoot, newItem: NodeRoot): Boolean {
        return oldItem.stableId == newItem.stableId
    }

    override fun areContentsTheSame(oldItem: NodeRoot, newItem: NodeRoot): Boolean {
        return oldItem == newItem
    }
}

class RootAdapter : ListAdapter<NodeRoot, RootViewHolder>(ItemCallbackImpl()) {
    companion object {
        private const val TYPE_VERTICAL = 1
        private const val TYPE_HORIZONTAL = 2
    }

    var clickListener: RootClickListener? = null
    var verticalCount: Int = 0
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = currentList[position].stableId.toLong()

    override fun getItemViewType(position: Int): Int = when {
        verticalCount == 0 -> TYPE_VERTICAL
        position < verticalCount -> TYPE_VERTICAL
        else -> TYPE_HORIZONTAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RootViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_explorer_card, parent, false)
        val holder = RootViewHolder(itemView)
        itemView.setOnClickListener {
            val item = currentList[holder.bindingAdapterPosition]
            clickListener?.onRootClick(item)
        }
        if (viewType == TYPE_HORIZONTAL) holder.makeHorizontal()
        return holder
    }

    override fun onBindViewHolder(holder: RootViewHolder, position: Int) {
        holder.bind(currentList[position], position)
    }

    interface RootClickListener {
        fun onRootClick(item: NodeRoot)
    }
}