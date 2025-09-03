package app.atomofiron.searchboxapp.injectable.store

import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.searchboxapp.model.explorer.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExplorerStore {

    private val currentTab = MutableStateFlow<NodeTabKey?>(null)
    private val deepestNodes = mutableMapOf<NodeTabKey, Node?>()
    private val checkedLists = mutableMapOf<NodeTabKey, List<Node>?>()
    private val currentLists = mutableMapOf<NodeTabKey, List<Node>?>()
    private val _currentNode = MutableStateFlow<Node?>(null)
    private val _storageRoot = MutableStateFlow<Node?>(null)
    private val _checked = MutableStateFlow<List<Node>>(listOf())
    private val _alerts = EventFlow<NodeError>()
    private val _removed = EventFlow<Node>()
    private val _deleted = EventFlow<List<Node>>()
    private val _updated = EventFlow<Node>()

    var currentItems = listOf<Node>()
        private set
    val currentNode: StateFlow<Node?> = _currentNode
    val storageRoot: StateFlow<Node?> = _storageRoot
    val checked: StateFlow<List<Node>> = _checked
    val alerts: Flow<NodeError> = _alerts
    val removed: Flow<Node> = _removed
    val deleted: Flow<List<Node>> = _deleted
    val updated: Flow<Node> = _updated

    fun setCurrentItems(tab: NodeTabKey, items: List<Node>) {
        currentLists[tab] = items
        updateCurrentItems(tab)
    }

    fun emitChecked(tab: NodeTabKey, items: List<Node>) {
        checkedLists[tab] = items
        updateChecked(tab)
    }

    fun setDeepestNode(tab: NodeTabKey, node: Node?) {
        deepestNodes[tab] = node
        updateDeepest(tab)
    }

    fun setCurrentTab(tab: NodeTabKey) {
        currentTab.value = tab
        updateChecked(tab)
        updateCurrentItems(tab)
        updateDeepest(tab)
    }

    fun setStorageRoot(item: Node) {
        _storageRoot.value = item
    }

    suspend fun emitUpdate(item: Node) = _updated.emit(item)

    suspend fun emitRemoved(item: Node) = _removed.emit(item)

    suspend fun emitDeleted(items: List<Node>) = _deleted.emit(items)

    private fun updateChecked(tab: NodeTabKey? = currentTab.value) {
        tab ?: return
        checkedLists.takeIf { tab == currentTab.value }
            ?.let { it[tab] }
            ?.let { _checked.value = it }
    }

    private fun updateDeepest(tab: NodeTabKey? = currentTab.value) {
        tab ?: return
        deepestNodes.takeIf { tab == currentTab.value }
            ?.let { it[tab] }
            .let { _currentNode.value = it }
    }

    private fun updateCurrentItems(tab: NodeTabKey) {
        currentLists.takeIf { tab == currentTab.value }
            ?.let { it[tab] }
            ?.let { currentItems = it }
    }
}