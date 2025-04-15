package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.searchboxapp.model.explorer.*
import kotlinx.coroutines.flow.MutableStateFlow

class ExplorerStore {
    var currentItems = listOf<Node>()
        private set
    val current = MutableStateFlow<Node?>(null)
    val searchTargets = MutableStateFlow<List<Node>>(listOf())
    val alerts = EventFlow<NodeError>()
    val removed = EventFlow<Node>()
    val updated = EventFlow<Node>()

    fun setCurrentItems(items: List<Node>) {
        currentItems = items
    }

    suspend fun emitUpdate(item: Node) = updated.emit(item)
}