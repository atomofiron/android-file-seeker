package app.atomofiron.searchboxapp.custom.view.dock

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.ListAdapter
import app.atomofiron.fileseeker.databinding.ItemDockBinding
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemConfig

class DockAdapter(private val selectListener: (DockItem) -> Unit) : ListAdapter<DockItem, DockItemHolder>(DockItemCallback()) {

    var itemConfig = DockItemConfig.Stub
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = currentList[position].id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DockItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDockBinding.inflate(inflater, parent, false)
        val holder = DockItemHolder(binding, selectListener)
        holder.itemView.tag = holder // hello 2008
        return holder
    }

    override fun onBindViewHolder(holder: DockItemHolder, position: Int) {
        holder.itemView.updateLayoutParams {
            width = itemConfig.width
            height = itemConfig.height
        }
        holder.bind(currentList[position], itemConfig)
    }
}
