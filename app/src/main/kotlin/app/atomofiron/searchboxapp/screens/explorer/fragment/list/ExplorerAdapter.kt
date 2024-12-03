package app.atomofiron.searchboxapp.screens.explorer.fragment.list

import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerHolder
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerItemViewFactory
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerItemViewFactory.*
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.ExplorerSeparatorHolder
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.NodeCallback
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.RecycleItemViewFactory
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.isDot

class ExplorerAdapter : ListAdapter<Node, GeneralHolder<Node>>(AsyncDifferConfig.Builder(NodeCallback()).build()) {

    lateinit var itemActionListener: ExplorerItemActionListener
    lateinit var separatorClickListener: (Node) -> Unit

    private lateinit var composition: ExplorerItemComposition
    private var viewCacheLimit = 5 // RecycledViewPool.DEFAULT_MAX_SCRAP

    private lateinit var viewFactory: RecycleItemViewFactory

    init {
        setHasStableIds(true)
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
    }

    override fun getItemViewType(position: Int): Int {
        val item = currentList[position]
        return when {
            item.isDot() -> SeparatorNodeItem
            item.isCurrent -> CurrentOpenedNodeItem
            item.isOpened -> OpenedNodeItem
            else -> NodeItem
        }.ordinal
    }

    override fun getItemId(position: Int): Long = currentList[position].uniqueId.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeneralHolder<Node> {
        if (parent.childCount > viewCacheLimit) {
            viewCacheLimit = parent.childCount
            parent as RecyclerView
            parent.recycledViewPool.setMaxRecycledViews(NodeItem.ordinal, viewCacheLimit)
            viewFactory.setLimit(viewCacheLimit)
        }
        val enum = ExplorerItemViewFactory.values()[viewType]
        val view = viewFactory.getOrCreate(enum.layoutId, parent)
        return enum.createHolder(view)
    }

    override fun onBindViewHolder(holder: GeneralHolder<Node>, position: Int) {
        val item = getItem(position)
        holder.bind(item, position)
        when (holder) {
            is ExplorerSeparatorHolder -> holder.setOnClickListener(separatorClickListener)
            is ExplorerHolder -> {
                holder.setOnItemActionListener(itemActionListener)
                holder.bindComposition(composition)
            }
        }
    }

    override fun onViewAttachedToWindow(holder: GeneralHolder<Node>) {
        super.onViewAttachedToWindow(holder)
        if (holder.bindingAdapterPosition > 0) {
            itemActionListener.onItemVisible(getItem(holder.bindingAdapterPosition))
        }
    }
}