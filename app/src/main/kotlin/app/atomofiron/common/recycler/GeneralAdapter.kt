package app.atomofiron.common.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

const val UNDEFINED = -1

abstract class GeneralAdapter<D : Any, H : GeneralHolder<D>>(
    itemCallback: DiffUtil.ItemCallback<D>? = null,
    itemUpdater: (D.(new: D) -> D)? = null,
) : RecyclerView.Adapter<H>() {

    private val mutableItems = mutableListOf<D>()
    private val differ = itemCallback?.let { CoroutineListDiffer(mutableItems, this, it, itemUpdater) }
    val items: List<D> = mutableItems

    override fun getItemCount(): Int = mutableItems.size

    operator fun get(position: Int) = mutableItems[position]

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H {
        val inflater = LayoutInflater.from(parent.context)
        return onCreateViewHolder(parent, viewType, inflater)
    }

    abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): H

    override fun onBindViewHolder(holder: H, position: Int) = holder.bind(mutableItems[position], position)

    fun submit(items: List<D>) {
        val differ = differ
        if (differ == null) {
            mutableItems.clear()
            mutableItems.addAll(items)
            notifyDataSetChanged()
        } else {
            differ.submit(mutableItems, items)
        }
    }

    fun submit(item: D, index: Int = UNDEFINED) {
        val differ = differ
        if (differ == null) {
            mutableItems[index] = item
            notifyItemChanged(index)
        } else {
            differ.submit(item, index)
        }
    }

    fun addListListener(listener: CoroutineListDiffer.ListListener<D>) {
        differ?.addListener(listener)
    }
}