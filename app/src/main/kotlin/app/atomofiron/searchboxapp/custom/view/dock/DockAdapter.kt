package app.atomofiron.searchboxapp.custom.view.dock

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import app.atomofiron.fileseeker.databinding.ItemDockBinding
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemCallback
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemHolder
import app.atomofiron.searchboxapp.custom.view.dock.popup.DockPopupConfig

class DockAdapter(
    private val selectListener: (DockItem) -> Unit,
) : ListAdapter<DockItem, DockItemHolder>(DockItemCallback()) {

    private var config: DockPopupConfig? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = currentList[position].id.value

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DockItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDockBinding.inflate(inflater, parent, false)
        val holder = DockItemHolder(binding, selectListener)
        holder.itemView.tag = holder // hello 2008
        return holder
    }

    override fun onBindViewHolder(holder: DockItemHolder, position: Int) {
        holder.bind(currentList[position], config)
    }

    fun set(config: DockPopupConfig) {
        if (config != this.config) {
            this.config = config
            notifyDataSetChanged()
        }
    }
}
