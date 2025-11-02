package app.atomofiron.searchboxapp.di.dependencies.store

import app.atomofiron.common.util.flow.throttleLatest
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.finder.SearchStatus
import app.atomofiron.searchboxapp.model.finder.GenericSearchTask
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
    private val _tasksFlow = MutableStateFlow(listOf<GenericSearchTask>())
    val tasksFlow = _tasksFlow.throttleLatest(duration = 100L)
    val tasks: List<GenericSearchTask> get() = _tasksFlow.value

    operator fun invoke(block: suspend FinderStore.() -> Unit) = scope.launch { block() }

    suspend fun add(item: GenericSearchTask) {
        _tasksFlow.updateList {
            add(item)
        }
    }

    suspend fun update(uuid: UUID, state: SearchStatus, error: String? = null) {
        _tasksFlow.updateList {
            val index = indexOfFirst { it.uuid == uuid }
            val current = getOrNull(index)
            when {
                current == null -> Unit
                current.status.order >= state.order -> Unit
                else -> set(index, current.copy(status = state, error = error ?: current.error))
            }
        }
    }

    suspend fun drop(item: GenericSearchTask) {
        _tasksFlow.updateList {
            remove(item)
        }
    }

    suspend fun addOrUpdate(item: GenericSearchTask) {
        _tasksFlow.updateList {
            when (val index = indexOfFirst { it.uuid == item.uuid }) {
                -1 -> add(item)
                else -> set(index, item)
            }
        }
    }

    suspend fun setSorting(id: Int, sorting: NodeSorting) {
        _tasksFlow.updateList {
            val index = indexOfFirst { it.uniqueId == id }
            val task = getOrNull(index) ?: return
            val result = task.result as? SearchResult.Files
            result ?: return
            set(index, task.copy(result = result.copy(sorting = sorting)))
        }
    }

    suspend fun deleteResultFromTasks(item: Node) {
        _tasksFlow.updateList {
            forEachIndexed { index, task ->
                val result = task.result as? SearchResult.Files
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