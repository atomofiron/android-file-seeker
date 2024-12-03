package app.atomofiron.common.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

abstract class GeneralAdapter<H : GeneralHolder<D>, D : Any> : RecyclerView.Adapter<H>() {
    companion object {
        private const val UNKNOWN = -1
    }
    protected val items: MutableList<D> = ArrayList()

    protected open val useDiffUtils = false
    protected open fun getDiffUtilCallback(old: List<D>, new: List<D>): DiffUtil.Callback? = null

    override fun getItemCount(): Int = items.size

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H {
        val inflater = LayoutInflater.from(parent.context)
        return onCreateViewHolder(parent, viewType, inflater)
    }

    abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): H

    override fun onBindViewHolder(holder: H, position: Int) {
        holder.bind(items[position], position)
    }

    open fun setItems(new: List<D>) {
        if (useDiffUtils) {
            val old = ArrayList<D>()
            old.addAll(items)
            items.clear()
            items.addAll(new)
            val callback = getDiffUtilCallback(old, new)!!
            val util = DiffUtil.calculateDiff(callback, false)
            util.dispatchUpdatesTo(this)
        } else {
            items.clear()
            items.addAll(new)
            notifyDataSetChanged()
        }
    }

    open fun setItem(item: D) {
        val index = items.indexOf(item)
        if (index != UNKNOWN) {
            items[index] = item
            notifyItemChanged(index)
        }
    }

    fun removeItem(item: D) {
        val index = items.indexOf(item)
        if (index != UNKNOWN) {
            items.remove(item)
            notifyItemRemoved(index)
        }
    }

    fun insertItem(previous: D, item: D) {
        val index = this.items.indexOf(previous).inc()
        this.items.add(index, item)
        notifyItemInserted(index)
    }

    fun removeItems(items: List<D>) {
        val it = items.iterator()
        while (it.hasNext()) {
            val next = it.next()
            val index = this.items.indexOf(next)
            if (index != UNKNOWN) {
                this.items.removeAt(index)
                notifyItemRemoved(index)
            }
        }
    }

    fun notifyItems(items: List<D>) {
        var indexFirst = UNKNOWN
        var indexLast = UNKNOWN
        val lastCount = items.size.dec()

        for (i in items.indices) {
            if (indexFirst == UNKNOWN) {
                indexFirst = this.items.indexOf(items[i])
            }
            if (indexLast == UNKNOWN) {
                indexLast = this.items.indexOf(items[lastCount - i])
            }
            if (indexFirst != UNKNOWN && indexLast != UNKNOWN) {
                break
            }
        }

        if (indexFirst == UNKNOWN) {
            return
        }

        // caution: do not replace items, notify only

        notifyItemRangeChanged(indexFirst, indexLast.inc() - indexFirst)
    }

    fun insertItems(previous: D, items: List<D>) {
        val index = this.items.indexOf(previous).inc()
        this.items.addAll(index, items)
        notifyItemRangeInserted(index, items.size)
    }

    fun setItemAt(index: Int, item: D) {
        items[index] = item
        notifyItemChanged(index)
    }

    fun removeItem(index: Int) {
        items.removeAt(index)
        notifyItemRemoved(index)
    }

    fun insertItem(index: Int, item: D) {
        items.add(index, item)
        notifyItemInserted(index)
    }
}