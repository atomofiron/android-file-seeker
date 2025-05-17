package app.atomofiron.common.recycler

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.recycler.GeneralAdapter.Companion.UNDEFINED
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// todo add and use 2-thread-safe mutable list for updates

class CoroutineListDiffer<I : Any>(
    private val adapter: RecyclerView.Adapter<*>,
    private val itemCallback: DiffUtil.ItemCallback<I>,
    listener: ListListener<I>? = null,
) {
    private val listeners = mutableListOf<ListListener<I>>()
    private var updated = mutableListOf<I>()
    private val scope = CoroutineScope(Dispatchers.Default)
    private var counter = 0
    private var actualList = mutableListOf<I>()
    private var isCalculating = false

    init {
        listener?.let { listeners.add(it) }
    }

    fun submit(current: List<I>, new: List<I>) {
        updated.clear()
        isCalculating = true
        val counter = ++counter
        val old = current.toMutableList()
        actualList.clear()
        actualList.addAll(new)
        scope.launch {
            val result = DiffUtil.calculateDiff(DiffCallback(itemCallback, old, new))
            withContext(Dispatchers.Main) {
                if (counter != this@CoroutineListDiffer.counter) {
                    return@withContext
                }
                isCalculating = false
                result.dispatchUpdatesTo(adapter)
                if (updated.isNotEmpty()) {
                    for (newer in updated) {
                        val index = actualList.indexOfFirst { itemCallback.areItemsTheSame(it, newer) }
                        if (index >= 0) {
                            actualList[index] = newer
                            adapter.notifyItemChanged(index)
                        }
                    }
                    updated.clear()
                }
                listeners.forEach { it.onCurrentListChanged(actualList) }
            }
        }
    }

    fun submit(item: I, index: Int = UNDEFINED) {
        val actualList = actualList
        val itemIndex = when {
            isCalculating -> return updated.indexOfFirst { itemCallback.areItemsTheSame(it, item) }.let {
                when {
                    it >= 0 -> updated[it] = item
                    else -> updated.add(item)
                }
            }
            index > UNDEFINED -> index
            else -> actualList.indexOfFirst { itemCallback.areItemsTheSame(it, item) }
                .also { if (it < 0) return }
        }
        actualList[itemIndex] = item
        adapter.notifyItemChanged(itemIndex)
        listeners.forEach { it.onItemChanged(itemIndex, item) }
    }

    fun addListener(listener: ListListener<I>): Boolean {
        return !listeners.contains(listener).also {
            if (!it) listeners.add(listener)
        }
    }

    fun removeListener(listener: ListListener<I>): Boolean = listeners.remove(listener)

    interface ListListener<I> {
        fun onItemChanged(index: Int, item: I)
        fun onCurrentListChanged(current: List<I>)
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