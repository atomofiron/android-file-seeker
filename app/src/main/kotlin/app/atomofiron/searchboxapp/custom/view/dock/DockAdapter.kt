package app.atomofiron.searchboxapp.custom.view.dock

import android.util.Size
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.ListAdapter
import app.atomofiron.fileseeker.databinding.ItemDockBinding

class DockAdapter(private val selectListener: (DockItem) -> Unit) : ListAdapter<DockItem, DockItemHolder>(DockItemCallback()) {

    var itemSize = Size(MATCH_PARENT, WRAP_CONTENT)
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
            width = itemSize.width
            height = itemSize.height
        }
        holder.bind(currentList[position])
    }
}
