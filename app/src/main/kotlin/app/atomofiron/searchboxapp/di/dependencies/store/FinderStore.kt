package app.atomofiron.searchboxapp.di.dependencies.store

import app.atomofiron.common.util.extension.set
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.finder.GenericSearchTask
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.finder.SearchStatus
import app.atomofiron.searchboxapp.utils.mutate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
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
    val tasksFlow: Flow<List<GenericSearchTask>> = _tasksFlow//.throttleLatest(duration = 100L)
    val tasks: List<GenericSearchTask> get() = _tasksFlow.value

    operator fun invoke(block: suspend FinderStore.() -> Unit) = scope.launch { block() }

    suspend fun add(item: GenericSearchTask) {
        updateTasks { add(item) }
    }

    suspend fun update(uuid: UUID, state: SearchStatus, error: String? = null) {
        update(uuid) {
            when {
                status.order >= state.order -> this
                else -> copy(status = state, error = error ?: error)
            }
        }
    }

    suspend fun drop(uuid: UUID) = update(uuid) { null }

    suspend fun addOrUpdate(item: GenericSearchTask) {
        updateTasks {
            set(item) { it.uuid == item.uuid }
        }
    }

    suspend fun setSorting(id: Int, sorting: NodeSorting) {
        updateTasks {
            val index = indexOfFirst { it.uniqueId == id }
            val task = getOrNull(index) ?: return
            val result = task.result as? SearchResult.Files
            result ?: return
            set(index, task.copy(result = result.copy(sorting = sorting)))
        }
    }

    suspend fun deleteResultFromTasks(items: List<Node>) {
        if (items.isEmpty()) {
            return
        }
        updateTasks {
            forEachIndexed { index, task ->
                val result = task.result as? SearchResult.Files
                result ?: return@forEachIndexed
                val new = result.removeItems(items)
                if (new !== result) {
                    this[index] = task.copy(result = new)
                }
            }
        }
    }

    private suspend inline fun updateTasks(action: MutableList<GenericSearchTask>.() -> Unit) {
        mutex.withLock {
            _tasksFlow.value = _tasksFlow.value.toMutableList().apply(action)
        }
    }

    suspend fun update(uuid: UUID, action: GenericSearchTask.() -> GenericSearchTask?) {
        mutex.withLock {
            val index = tasks.indexOfFirst { it.uuid == uuid }
            val task = tasks.getOrNull(index) ?: return
            val new = task.action()
            if (new !== task) {
                _tasksFlow.value = tasks.mutate {
                    when (new) {
                        null -> removeAt(index)
                        else -> set(index, new)
                    }
                }
            }
        }
    }
}