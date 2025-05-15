package app.atomofiron.searchboxapp.custom.view.dock

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.databinding.ItemDockBinding
import app.atomofiron.searchboxapp.custom.view.dock.item.DockDiffCallback
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemConfig
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemHolder

class DockAdapter(
    private var config: DockItemConfig,
    private val selectListener: (DockItem) -> Unit,
) : RecyclerView.Adapter<DockItemHolder>() {

    private val items = mutableListOf<DockItem>()

    init {
        setHasStableIds(true)
    }

    override fun getItemCount() = items.size

    override fun getItemId(position: Int): Long = items[position].id.value

    override fun getItemViewType(position: Int): Int = position

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

    fun set(config: DockItemConfig) {
        if (config != this.config) {
            this.config = config
            notifyDataSetChanged()
        }
    }

    fun submit(items: List<DockItem>) {
        DiffUtil.calculateDiff(DockDiffCallback(old = this.items, new =  items))
            .dispatchUpdatesTo(this)
        this.items.clear()
        this.items.addAll(items)
    }
}
