package app.atomofiron.searchboxapp.screens.result.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition

class ResultAdapter : GeneralAdapter<ResultItem, ResultsHolder<ResultItem>>(ResultDiffUtilCallback) {

    lateinit var itemActionListener: ResultItemActionListener

    private lateinit var composition: ExplorerItemComposition

    fun setResult(results: SearchResult.Files) {
        val dirCount = results.matches.count { it.item.isDirectory }
        val fileCount = results.matches.size - dirCount
        val items = buildList(results.matches.size.inc()) {
            add(ResultItem.Header(dirCount, fileCount, results.errors.size))
            results.matches.forEach {
                add(ResultItem.Item(it))
            }
        }
        submit(items)
    }

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
    }

    override fun getItemViewType(position: Int): Int = get(position).viewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): ResultsHolder<ResultItem> {
        return ResultViewType(viewType).createHolder(parent, inflater) as ResultsHolder<ResultItem>
    }

    override fun onBindViewHolder(holder: ResultsHolder<ResultItem>, position: Int) {
        holder.listener = itemActionListener
        super.onBindViewHolder(holder, position)
        if (holder is ResultsItemHolder) {
            holder.setOnItemActionListener(itemActionListener)
            holder.bindComposition(composition)
        }
    }
}