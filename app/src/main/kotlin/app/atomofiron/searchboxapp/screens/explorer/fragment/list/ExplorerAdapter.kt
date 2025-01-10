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
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ItemVisibilityDelegate
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.NodeCallback
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.RecycleItemViewFactory
import app.atomofiron.searchboxapp.utils.ExplorerUtils.isDot

class ExplorerAdapter(
    private val itemActionListener: ExplorerItemActionListener,
    private val separatorClickListener: (Node) -> Unit,
) : ListAdapter<Node, GeneralHolder<Node>>(AsyncDifferConfig.Builder(NodeCallback()).build()) {

    private lateinit var composition: ExplorerItemComposition
    private var viewCacheLimit = 5 // RecycledViewPool.DEFAULT_MAX_SCRAP

    private lateinit var viewFactory: RecycleItemViewFactory

    private val itemVisibilityDelegate = ItemVisibilityDelegate(this, itemActionListener)
    val visibleItems = itemVisibilityDelegate.visibleItems

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
        val enum = ExplorerItemViewFactory.entries[viewType]
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

    override fun onViewAttachedToWindow(holder: GeneralHolder<Node>) = itemVisibilityDelegate.onItemAttached(holder)

    override fun onViewDetachedFromWindow(holder: GeneralHolder<Node>) = itemVisibilityDelegate.onItemDetached(holder)
}