package app.atomofiron.searchboxapp.di.dependencies.store

import app.atomofiron.common.util.flow.throttleLatest
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.finder.SearchState
import app.atomofiron.searchboxapp.model.finder.SearchTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class FinderStore(
    private val scope: CoroutineScope,
) {
    private val mutex = Mutex()
    private val mutableTasks = MutableStateFlow(listOf<SearchTask>())
    val tasksFlow = mutableTasks.throttleLatest(duration = 100L)
    val tasks: List<SearchTask> get() = mutableTasks.value

    operator fun invoke(block: suspend FinderStore.() -> Unit) {
        scope.launch { block() }
    }

    suspend fun add(item: SearchTask) {
        mutableTasks.updateList {
            add(item)
        }
    }

    suspend fun update(uuid: UUID, state: SearchState, error: String? = null) {
        mutableTasks.updateList {
            val index = indexOfFirst { it.uuid == uuid }
            val current = getOrNull(index)
            when {
                current == null -> Unit
                current.state.order >= state.order -> Unit
                else -> set(index, current.copy(state = state, error = error ?: current.error))
            }
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

    suspend fun setSorting(id: Int, sorting: NodeSorting) {
        mutableTasks.updateList {
            val index = indexOfFirst { it.uniqueId == id }
            val task = getOrNull(index)
            val result = task?.result as? SearchResult.FinderResult
            when (null) {
                task, result -> return
                else -> set(index, task.copy(result = result.copy(sorting = sorting)))
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

    private suspend inline fun <T> MutableStateFlow<List<T>>.updateList(action: MutableList<T>.() -> Unit) {
        mutex.withLock {
            value = value.toMutableList().apply(action)
        }
    }
}