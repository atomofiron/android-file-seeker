package app.atomofiron.searchboxapp.screens.finder.fragment.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.screens.finder.di.history.ItemHistory

class HistoryAdapter(
    private val listener: OnItemClickListener,
) : ListAdapter<ItemHistory, HistoryHolder>(HistoryItemCallback), HistoryHolder.OnItemActionListener {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_history, parent, false)
        return HistoryHolder(view, this)
    }

    override fun getItemId(position: Int): Long = getItem(position).id

    override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
        val item = getItem(position)
        holder.onBind(item.query, item.pinned)
    }

    override fun onItemClick(position: Int) = listener.onItemClick(getItem(position))

    override fun onItemPin(position: Int) = listener.onItemPin(getItem(position))

    override fun onItemRemove(position: Int) = listener.onItemRemove(getItem(position))

    interface OnItemClickListener {
        fun onItemClick(item: ItemHistory)
        fun onItemPin(item: ItemHistory)
        fun onItemRemove(item: ItemHistory)
    }
}
