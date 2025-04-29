package app.atomofiron.common.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.UnreachableException

abstract class GeneralAdapter<D : Any, H : GeneralHolder<D>> : RecyclerView.Adapter<H>(), AsyncListDiffer.ListListener<D> {
    companion object {
        const val UNDEFINED = -1
    }

    private val mutableItems = mutableListOf<D>()
    val items: List<D> = mutableItems
    private var updated = mutableListOf<D>()

    private val itemCallback: DiffUtil.ItemCallback<D>? = getItemCallback()
    private var isCalculating = false
    private val differ by lazy(LazyThreadSafetyMode.NONE) {
        itemCallback?.let { callback ->
            AsyncListDiffer(AdapterListUpdateCallback(this), AsyncDifferConfig.Builder(callback).build())
        }
    }

    init {
        differ?.addListListener(this)
    }

    override fun getItemCount(): Int = mutableItems.size

    operator fun get(position: Int) = mutableItems[position]

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H {
        val inflater = LayoutInflater.from(parent.context)
        return onCreateViewHolder(parent, viewType, inflater)
    }

    abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): H

    override fun onBindViewHolder(holder: H, position: Int) = holder.bind(mutableItems[position], position)

    override fun onCurrentListChanged(previousList: List<D>, currentList: List<D>) {
        isCalculating = false
        val newItems: List<D> = if (updated.isEmpty()) currentList else currentList.toMutableList().apply {
            val callback = itemCallback ?: throw UnreachableException()
            for (newer in updated) {
                val index = indexOfFirst { callback.areItemsTheSame(it, newer) }
                if (index >= 0) set(index, newer)
            }
        }
        mutableItems.clear()
        mutableItems.addAll(newItems)
    }

    // inheritor's fields init after super
    protected open fun getItemCallback(): DiffUtil.ItemCallback<D>? = null

    fun submit(items: List<D>) {
        val differ = differ
        if (differ == null) {
            mutableItems.clear()
            mutableItems.addAll(items)
            notifyDataSetChanged()
        } else {
            isCalculating = true
            differ.submitList(items)
            updated.clear()
        }
    }

    fun submit(item: D, itemIndex: Int = UNDEFINED) {
        val index = when {
            itemIndex > UNDEFINED -> itemIndex
            itemCallback == null -> throw UnsupportedOperationException()
            isCalculating -> {
                val index = updated.indexOfFirst { itemCallback.areItemsTheSame(it, item) }
                if (index >= 0) updated.removeAt(index)
                updated.add(item)
                return
            }
            else -> mutableItems.indexOfFirst { itemCallback.areItemsTheSame(it, item) }
                .also { if (it < 0) return }
        }
        mutableItems[index] = item
        notifyItemChanged(index)
    }

    fun addListListener(listener: AsyncListDiffer.ListListener<D>) {
        differ?.addListListener(listener)
    }
}