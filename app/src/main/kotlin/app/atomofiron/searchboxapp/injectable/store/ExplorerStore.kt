package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.searchboxapp.model.explorer.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class ExplorerStore {
    var currentItems = listOf<Node>()
        private set
    private val currentTab = MutableSharedFlow<NodeTabKey?>()
    private val deepestNodes = MutableStateFlow<Map<NodeTabKey,Node>>(emptyMap())
    val currentNode = combine(deepestNodes, currentTab) { nodes, tab -> nodes[tab] }
    val storageRoot = MutableStateFlow<Node?>(null)
    val searchTargets = MutableStateFlow<List<Node>>(listOf())
    val alerts = EventFlow<NodeError>()
    val removed = EventFlow<Node>()
    val updated = EventFlow<Node>()

    fun setCurrentItems(items: List<Node>) {
        currentItems = items
    }

    fun setStorageRoot(item: Node) {
        storageRoot.value = item
    }

    suspend fun emitUpdate(item: Node) = updated.emit(item)

    fun setDeepestNode(tab: NodeTabKey, node: Node?) {
        deepestNodes.value = deepestNodes.value.toMutableMap().apply {
            if (node == null) remove(tab) else set(tab, node)
        }
    }

    suspend fun setCurrentTab(tab: NodeTabKey) = currentTab.emit(tab)
}