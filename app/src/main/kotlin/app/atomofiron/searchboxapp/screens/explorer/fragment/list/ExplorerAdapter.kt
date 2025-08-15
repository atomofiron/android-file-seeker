package app.atomofiron.searchboxapp.screens.explorer.fragment.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerHolder
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerItemViewFactory
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerItemViewFactory.CurrentOpenedNodeItem
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerItemViewFactory.NodeItem
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerItemViewFactory.OpenedNodeItem
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerItemViewFactory.SeparatorNodeItem
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerSeparatorHolder
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ItemVisibilityDelegate
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.NodeCallback
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.RecycleItemViewFactory
import app.atomofiron.searchboxapp.utils.ExplorerUtils.isSeparator

class ExplorerAdapter(
    private val itemActionListener: ExplorerItemActionListener,
    private val separatorClickListener: (Node) -> Unit,
) : GeneralAdapter<Node, GeneralHolder<Node>>(NodeCallback, Node::updater) {

    private lateinit var composition: ExplorerItemComposition
    private var viewCacheLimit = 5 // RecycledViewPool.DEFAULT_MAX_SCRAP

    private lateinit var viewFactory: RecycleItemViewFactory

    private val itemVisibilityDelegate = ItemVisibilityDelegate(this, itemActionListener)
    val visibleItems = itemVisibilityDelegate.visibleItems

    init {
        setHasStableIds(true)
        registerAdapterDataObserver(ChangeListener())
    }

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
        notifyDataSetChanged()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.itemAnimator = null
        viewFactory = RecycleItemViewFactory(recyclerView.context, R.layout.item_explorer)
        viewFactory.generate(NodeItem.layoutId, recyclerView)
        ExplorerItemViewFactory.entries.forEach {
            recyclerView.recycledViewPool.setMaxRecycledViews(it.viewType, it.cache)
        }
        recyclerView.setItemViewCacheSize(32)
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return when {
            item.isSeparator() -> SeparatorNodeItem
            item.isDeepest -> CurrentOpenedNodeItem
            item.isOpened -> OpenedNodeItem
            else -> NodeItem
        }.viewType
    }

    override fun getItemId(position: Int): Long = items[position].uniqueId.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): GeneralHolder<Node> {
        if (parent.childCount > viewCacheLimit) {
            viewCacheLimit = parent.childCount
            parent as RecyclerView
            parent.recycledViewPool.setMaxRecycledViews(NodeItem.viewType, viewCacheLimit)
            viewFactory.setLimit(viewCacheLimit)
        }
        val enum = ExplorerItemViewFactory.entries[viewType]
        val view = viewFactory.getOrCreate(enum.layoutId, parent)
        return enum.createHolder(view)
    }

    override fun onBindViewHolder(holder: GeneralHolder<Node>, position: Int) {
        val item = items[position]
        holder.bind(item, position)
        when (holder) {
            is ExplorerSeparatorHolder -> holder.setOnClickListener(separatorClickListener)
            is ExplorerHolder -> {
                holder.setOnItemActionListener(itemActionListener)
                holder.bindComposition(composition)
            }
        }
    }

    override fun onViewAttachedToWindow(holder: GeneralHolder<Node>) = itemVisibilityDelegate.onItemAttached(holder)

    override fun onViewDetachedFromWindow(holder: GeneralHolder<Node>) = itemVisibilityDelegate.onItemDetached(holder)

    private inner class ChangeListener : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = onEvent(positionStart)
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = onEvent(positionStart)
        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            onEvent(fromPosition)
            onEvent(toPosition)
        }
        private fun onEvent(position: Int) {
            if (position > 0) {
                // to update decoration offsets
                notifyItemChanged(position.dec())
            }
        }
    }
}

private fun Node.updater(new: Node): Node {
    return new.takeIf { it.isOpened == isOpened && it.isDeepest == isDeepest }
        ?: new.copy(isDeepest = isDeepest, children = new.children?.copy(isOpened = isOpened))
}
