package app.atomofiron.common.recycler

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.extension.copy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CoroutineListDiffer<I : Any>(
    private val actualList: MutableList<I>,
    private val adapter: RecyclerView.Adapter<*>,
    private val itemId: (I.() -> Int)? = null,
    private val itemCallback: DiffUtil.ItemCallback<I>,
    private val itemGeneration: (I.() -> Int)? = null,
    private val itemUpdater: (I.(new: I) -> I)? = null,
    private val detectMoves: Boolean = true,
    listener: ListListener<I>? = null,
) {
    private val listeners = mutableListOf<ListListener<I>>()
    private var updated = mutableMapOf<Int, I>()
    private val scope = CoroutineScope(Dispatchers.Default)
    private var counter = 0
    private var isCalculating = false

    init {
        listener?.let { listeners.add(it) }
    }

    fun submit(new: List<I>, isNew: Boolean) {
        if (isNew) {
            counter++
            updated.clear()
            isCalculating = false
            actualList.clear()
            actualList.addAll(new)
            adapter.notifyDataSetChanged()
            listeners.forEach { it.onCurrentListChanged(new) }
        } else {
            submit(old = actualList.copy(), new)
        }
    }

    private fun submit(old: List<I>, new: List<I>) {
        isCalculating = true
        val currentCounter = ++counter
        scope.launch {
            val result = DiffUtil.calculateDiff(DiffCallback(itemCallback, old, new), detectMoves)
            withContext(Dispatchers.Main) {
                if (currentCounter != counter) {
                    return@withContext
                }
                isCalculating = false
                actualList.clear()
                actualList.addAll(new)
                result.dispatchUpdatesTo(adapter)
                new.firstOrNull()?.syncByGeneration()
                if (updated.isNotEmpty() && itemId != null) {
                    for (i in actualList.indices) {
                        val item = actualList[i]
                        val newer = updated[itemId(item)]
                        newer ?: continue
                        actualList[i] = itemUpdater?.invoke(item, newer) ?: newer
                        adapter.notifyItemChanged(i)
                    }
                    itemGeneration ?: updated.clear()
                }
                listeners.forEach { it.onCurrentListChanged(actualList.copy()) }
            }
        }
    }

    fun submit(item: I, index: Int = UNDEFINED) {
        item.syncByGeneration()
        itemId?.invoke(item)
            ?.let { updated[it] = item }
        val itemIndex = when {
            index > UNDEFINED -> index
            else -> actualList.indexOfFirst { itemCallback.areItemsTheSame(it, item) }
        }
        if (itemIndex >= 0) {
            actualList[itemIndex] = item
            adapter.notifyItemChanged(itemIndex)
            listeners.forEach { it.onChanged(itemIndex, item) }
        }
    }

    fun addListener(listener: ListListener<I>): Boolean {
        return !listeners.contains(listener).also {
            if (!it) listeners.add(listener)
        }
    }

    fun removeListener(listener: ListListener<I>): Boolean = listeners.remove(listener)

    private fun I.syncByGeneration() {
        itemGeneration ?: return
        updated.values
            .firstOrNull()
            ?.itemGeneration()
            ?.takeIf { it < this.itemGeneration() }
            ?.let { updated.clear() }
    }

    interface ListListener<I> {
        fun onCurrentListChanged(current: List<I>)
        fun onChanged(index: Int, new: I) = Unit
    }

    private class DiffCallback<I : Any>(
        private val callback: DiffUtil.ItemCallback<I>,
        private val old: List<I>,
        private val new: List<I>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = old.size
        override fun getNewListSize() = new.size
        override fun areItemsTheSame(oldPosition: Int, newPosition: Int) = callback.areItemsTheSame(old[oldPosition], new[newPosition])
        override fun areContentsTheSame(oldPosition: Int, newPosition: Int) = callback.areContentsTheSame(old[oldPosition], new[newPosition])
    }
}

