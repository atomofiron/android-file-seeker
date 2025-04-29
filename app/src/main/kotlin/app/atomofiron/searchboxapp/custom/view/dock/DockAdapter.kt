package app.atomofiron.searchboxapp.custom.view.dock

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.databinding.ItemDockBinding
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemHolder
import app.atomofiron.searchboxapp.custom.view.dock.popup.DockPopupConfig

class DockAdapter(
    private val selectListener: (DockItem) -> Unit,
) : RecyclerView.Adapter<DockItemHolder>() {

    private var config: DockPopupConfig? = null
    private val items = mutableListOf<DockItem>()

    init {
        setHasStableIds(true)
    }

    override fun getItemCount() = items.size

    override fun getItemId(position: Int): Long = items[position].id.value

    operator fun get(position: Int): DockItem = items[position]

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DockItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDockBinding.inflate(inflater, parent, false)
        val holder = DockItemHolder(binding, selectListener)
        holder.itemView.tag = holder // hello 2008
        return holder
    }

    override fun onBindViewHolder(holder: DockItemHolder, position: Int) {
        holder.bind(items[position], config)
    }

    fun set(config: DockPopupConfig) {
        if (config != this.config) {
            this.config = config
            notifyDataSetChanged()
        }
    }

    fun submit(items: List<DockItem>) {
        DiffUtil.calculateDiff(Callback(old = this.items, new =  items))
            .dispatchUpdatesTo(this)
        this.items.clear()
        this.items.addAll(items)
    }

    private class Callback(
        private val old: List<DockItem>,
        private val new: List<DockItem>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = old.size
        override fun getNewListSize() = new.size
        override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean = old[oldPosition].id == new[newPosition].id
        override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean = old[oldPosition] == new[newPosition]
    }
}
