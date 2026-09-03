package app.atomofiron.searchboxapp.screens.explorer.fragment.roots.options

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import app.atomofiron.searchboxapp.model.explorer.NodeRootOption
import app.atomofiron.searchboxapp.utils.inflater

private object ItemCallbackImpl : DiffUtil.ItemCallback<NodeRootOption>() {
    override fun areItemsTheSame(oldItem: NodeRootOption, newItem: NodeRootOption) = oldItem.similar(newItem)
    override fun areContentsTheSame(oldItem: NodeRootOption, newItem: NodeRootOption) = oldItem == newItem
}

class RootOptionAdapter(
    private val output: RootOptionListener,
) : ListAdapter<NodeRootOption, RootOptionViewHolder>(ItemCallbackImpl) {

    init {
        setHasStableIds(true)
    }

    fun set(item: NodeRootOption?) = when (item) {
        null -> submitList(emptyList())
        else -> submitList(listOf(item))
    }

    override fun getItemId(position: Int): Long = RootOptionItemViewFactory.entries[position].viewType.toLong()

    override fun getItemViewType(position: Int): Int = RootOptionItemViewFactory.entries[position].viewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RootOptionViewHolder {
        return RootOptionItemViewFactory.entries.find { it.viewType == viewType }!!
            .createHolder(parent.inflater(), output)
    }

    override fun onBindViewHolder(holder: RootOptionViewHolder, position: Int) {
        holder.bind(currentList[position], position)
    }

    interface RootOptionListener : RootOptionViewHolder.OnCameraToggleClickListener
}