package app.atomofiron.searchboxapp.di.dependencies.store

import app.atomofiron.common.util.extension.put
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.finder.GlobalSearchTask
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.mutate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.Uuid

@Singleton
class FinderStore @Inject constructor() {

    private val mutex = Mutex()
    val tasksFlow: StateFlow<List<GlobalSearchTask>>
        field = MutableStateFlow(listOf<GlobalSearchTask>())
    val tasks: List<GlobalSearchTask> get() = tasksFlow.value

    suspend fun add(item: GlobalSearchTask) {
        updateTasks { add(item) }
    }

    suspend fun drop(uuid: Uuid) = update(uuid) { null }

    suspend fun addAll(items: List<GlobalSearchTask>) = updateTasks {
        addAll(items)
    }

    suspend fun addOrUpdate(item: GlobalSearchTask) = updateTasks {
        put(item) { it.uuid == item.uuid }
    }

    suspend fun setSorting(id: Int, sorting: NodeSorting): Rslt<Unit> {
        return updateTasks {
            val index = indexOfFirst { it.uniqueId == id }
            val task = getOrNull(index) ?: return Rslt.Err
            set(index, task.copy(sorting = sorting))
            Rslt.Ok
        }
    }

    suspend fun deleteResultFromTasks(items: List<Node>) {
        if (items.isEmpty()) {
            return
        }
        updateTasks {
            forEachIndexed { index, task ->
                val new = task.result.removeItems(items)
                if (new !== task.result) {
                    this[index] = task.copy(result = new)
                }
            }
        }
    }

    private suspend inline fun <R> updateTasks(action: MutableList<GlobalSearchTask>.() -> R): R {
        return mutex.withLock {
            val new = tasksFlow.value.toMutableList()
            val result = new.action()
            tasksFlow.value = new
            result
        }
    }

    suspend fun update(uuid: Uuid, action: GlobalSearchTask.() -> GlobalSearchTask?) {
        mutex.withLock {
            val index = tasks.indexOfFirst { it.uuid == uuid }
            val task = tasks.getOrNull(index) ?: return
            val new = task.action()
            if (new !== task) {
                tasksFlow.value = tasks.mutate {
                    when (new) {
                        null -> removeAt(index)
                        else -> set(index, new)
                    }
                }
            }
        }
    }
}