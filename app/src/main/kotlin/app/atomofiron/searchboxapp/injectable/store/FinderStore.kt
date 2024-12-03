package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.flow.throttleLatest
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.textviewer.SearchTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FinderStore(
    private val scope: CoroutineScope,
) {
    private val mutex = Mutex()
    private val mutableTasks = MutableStateFlow(listOf<SearchTask>())
    val tasksFlow = mutableTasks.throttleLatest(duration = 100L)
    val tasks: List<SearchTask> get() = mutableTasks.value

    suspend fun add(item: SearchTask) {
        mutableTasks.updateList {
            add(item)
        }
    }

    suspend fun drop(item: SearchTask) {
        mutableTasks.updateList {
            remove(item)
        }
    }

    suspend fun addOrUpdate(item: SearchTask) {
        mutableTasks.updateList {
            when (val index = indexOfFirst { it.uuid == item.uuid }) {
                -1 -> add(item)
                else -> set(index, item)
            }
        }
    }

    suspend fun deleteResultFromTasks(item: Node) {
        mutableTasks.updateList {
            forEachIndexed { index, task ->
                val result = task.result as? SearchResult.FinderResult
                result ?: return@forEachIndexed
                val new = result.removeItem(item)
                if (new !== result) {
                    this[index] = task.copyWith(new)
                }
            }
        }
    }

    private suspend fun <T> MutableStateFlow<List<T>>.updateList(action: MutableList<T>.(current: List<T>) -> Unit) {
        mutex.withLock {
            value = value.toMutableList().apply {
                action(value)
            }
        }
    }
}