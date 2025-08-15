package app.atomofiron.common.recycler

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.extension.copy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// todo add and use 2-thread-safe mutable list for updates

private const val DetectMoves = false

class CoroutineListDiffer<I : Any>(
    private val actualList: MutableList<I>,
    private val adapter: RecyclerView.Adapter<*>,
    private val itemCallback: DiffUtil.ItemCallback<I>,
    private val itemUpdater: (I.(new: I) -> I)? = null,
    listener: ListListener<I>? = null,
) {
    private val listeners = mutableListOf<ListListener<I>>()
    private var updated = mutableListOf<I>()
    private val scope = CoroutineScope(Dispatchers.Default)
    private var counter = 0
    private var isCalculating = false

    init {
        listener?.let { listeners.add(it) }
    }

    fun submit(current: List<I>, new: List<I>) {
        isCalculating = true
        val currentCounter = ++counter
        val old = current.copy()
        scope.launch {
            val result = DiffUtil.calculateDiff(DiffCallback(itemCallback, old, new), DetectMoves)
            withContext(Dispatchers.Main) {
                if (currentCounter != counter) {
                    return@withContext
                }
                isCalculating = false
                actualList.clear()
                actualList.addAll(new)
                result.dispatchUpdatesTo(adapter)
                if (updated.isNotEmpty()) {
                    for (newer in updated) {
                        val index = actualList.indexOfFirst { itemCallback.areItemsTheSame(it, newer) }
                        if (index >= 0) {
                            actualList[index] = itemUpdater?.invoke(actualList[index], newer) ?: newer
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
        val itemIndex = when {
            isCalculating -> return updated
                .indexOfFirst { itemCallback.areItemsTheSame(it, item) }
                .let { if (it >= 0) updated[it] = item else updated.add(item) }
            index > UNDEFINED -> index
            else -> actualList.indexOfFirst { itemCallback.areItemsTheSame(it, item) }
                .also { if (it < 0) return }
        }
        actualList[itemIndex] = item
        adapter.notifyItemChanged(itemIndex)
        listeners.forEach { it.onChanged(itemIndex, item) }
    }

    fun addListener(listener: ListListener<I>): Boolean {
        return !listeners.contains(listener).also {
            if (!it) listeners.add(listener)
        }
    }

    fun removeListener(listener: ListListener<I>): Boolean = listeners.remove(listener)

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

