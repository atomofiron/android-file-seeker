package app.atomofiron.searchboxapp.screens.result.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.common.util.extension.debugRequire
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ItemVisibilityDelegate

class ResultAdapter : GeneralAdapter<ResultItem, ResultsHolder<ResultItem>>(ResultDiffUtilCallback)
    , ItemVisibilityDelegate.ItemVisibilityListener<ResultItem>
{
    private val itemVisibilityDelegate = ItemVisibilityDelegate(this, this)

    var itemActionListener: ResultItemActionListener? = null
    private var composition: ExplorerItemComposition? = null

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
    }

    override fun getItemViewType(position: Int): Int = get(position).viewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): ResultsHolder<ResultItem> {
        val holder = ResultViewType(viewType).createHolder(parent, inflater) as ResultsHolder<ResultItem>
        debugRequire(itemActionListener != null)
        debugRequire(composition != null)
        when (holder) {
            is ResultsItemHolder -> holder.setOnItemActionListener(itemActionListener)
            is ResultsHeaderHolder -> holder.listener = itemActionListener
        }
        return holder
    }

    override fun onBindViewHolder(holder: ResultsHolder<ResultItem>, position: Int) {
        super.onBindViewHolder(holder, position)
        if (holder is ResultsItemHolder) {
            composition?.let { holder.bindComposition(it) }
        }
    }

    override fun onViewAttachedToWindow(holder: ResultsHolder<ResultItem>) = itemVisibilityDelegate.onItemAttached(holder)

    override fun onViewDetachedFromWindow(holder: ResultsHolder<ResultItem>) = itemVisibilityDelegate.onItemDetached(holder)

    override fun onItemsBecomeVisible(items: List<ResultItem>) {
        items.forEach {
            if (it is ResultItem.Item) {
                itemActionListener?.onItemVisible(it)
            }
        }
    }
}