package app.atomofiron.common.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class GeneralAdapter<D : Any, H : GeneralHolder<D>>(
    itemCallback: DiffUtil.ItemCallback<D>? = null,
) : RecyclerView.Adapter<H>(), CoroutineListDiffer.ListListener<D> {
    companion object {
        const val UNDEFINED = -1
    }

    private val differ = itemCallback?.let { CoroutineListDiffer(this, it, listener = this) }
    private val mutableItems = mutableListOf<D>()
    val items: List<D> = mutableItems

    override fun getItemCount(): Int = mutableItems.size

    operator fun get(position: Int) = mutableItems[position]

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H {
        val inflater = LayoutInflater.from(parent.context)
        return onCreateViewHolder(parent, viewType, inflater)
    }

    abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): H

    override fun onBindViewHolder(holder: H, position: Int) = holder.bind(mutableItems[position], position)

    override fun onItemChanged(index: Int, item: D) {
        mutableItems[index] = item
    }

    override fun onCurrentListChanged(current: List<D>) {
        mutableItems.clear()
        mutableItems.addAll(current)
    }

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