package app.atomofiron.searchboxapp.screens.explorer.fragment.roots

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.NodeRoot

private object ItemCallbackImpl : DiffUtil.ItemCallback<NodeRoot>() {
    override fun areItemsTheSame(oldItem: NodeRoot, newItem: NodeRoot): Boolean {
        return oldItem.stableId == newItem.stableId
    }

    override fun areContentsTheSame(oldItem: NodeRoot, newItem: NodeRoot): Boolean {
        return oldItem == newItem
    }
}

class RootAdapter(private val listener: RootClickListener) : ListAdapter<NodeRoot, RootViewHolder>(ItemCallbackImpl) {

    init {
        setHasStableIds(true)
    }

    private fun stableId(position: Int): Int = currentList[position].stableId

    override fun getItemId(position: Int): Long = stableId(position).toLong()

    override fun getItemViewType(position: Int): Int = stableId(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RootViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_explorer_card, parent, false)
        val holder = RootViewHolder(itemView)
        itemView.setOnClickListener {
            val item = currentList[holder.trueBindingAdapterPosition]
            listener.onRootClick(item)
        }
        return holder
    }

    override fun onBindViewHolder(holder: RootViewHolder, position: Int) {
        holder.bind(currentList[position], position)
    }

    interface RootClickListener {
        fun onRootClick(item: NodeRoot)
    }
}