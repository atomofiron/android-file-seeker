package app.atomofiron.searchboxapp.screens.result.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.decorator.ItemBackgroundDecorator

class ResultAdapter : GeneralAdapter<ResultItem, ResultsHolder>(ResultDiffUtilCallback) {
    companion object {
        private const val POSITION_HEADER = 0
        private const val TYPE_HEADER = 2
    }
    lateinit var itemActionListener: ResultItemActionListener

    private lateinit var composition: ExplorerItemComposition

    private val gravityDecorator = ItemGravityDecorator()
    private val backgroundDecorator = ItemBackgroundDecorator(evenNumbered = false)

    fun setResult(results: SearchResult.FinderResult) {
        val dirCount = results.matches.count { it.item.isDirectory }
        val fileCount = results.matches.size - dirCount
        val items = buildList(results.matches.size.inc()) {
            add(ResultItem.Header(dirCount, fileCount))
            results.matches.forEach {
                add(ResultItem.Item(it))
            }
        }
        submit(items)
    }

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
        backgroundDecorator.enabled = composition.visibleBg
    }

    override fun getItemViewType(position: Int): Int = when (position) {
        POSITION_HEADER -> TYPE_HEADER
        else -> super.getItemViewType(position)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.addItemDecoration(gravityDecorator)
        recyclerView.addItemDecoration(backgroundDecorator)
        backgroundDecorator.init(recyclerView.resources)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        recyclerView.removeItemDecoration(gravityDecorator)
        recyclerView.removeItemDecoration(backgroundDecorator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): ResultsHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val itemView = inflater.inflate(R.layout.item_header, parent, false)
                ResultsHeaderHolder(itemView)
            }
            else -> {
                val itemView = inflater.inflate(R.layout.item_explorer, parent, false)
                ResultsItemHolder(itemView)
            }
        }
    }

    override fun onBindViewHolder(holder: ResultsHolder, position: Int) = when (position) {
        POSITION_HEADER -> holder.bind(items[position], position)
        else -> {
            holder as ResultsItemHolder
            holder.setOnItemActionListener(itemActionListener)
            super.onBindViewHolder(holder, position)
            holder.bindComposition(composition)
            val item = items[position] as ResultItem.Item
            itemActionListener.onItemVisible(item)
        }
    }
}