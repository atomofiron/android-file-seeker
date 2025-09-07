package app.atomofiron.searchboxapp.di.dependencies.store

import android.os.Environment
import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.searchboxapp.model.explorer.*
import app.atomofiron.searchboxapp.model.explorer.NodeRoot.NodeRootType
import app.atomofiron.searchboxapp.utils.ExplorerUtils.asRoot
import app.atomofiron.searchboxapp.utils.ExplorerUtils.completePath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExplorerStore {

    val firstTab = NodeTabKey(index = 1)
    val middleTab = NodeTabKey(index = 0)
    val lastTab = NodeTabKey(index = 2)

    val internalStoragePath = Environment
        .getExternalStorageDirectory()
        .absolutePath
        .completePath(directory = true)

    private val deepestNodes = mutableMapOf<NodeTabKey, Node?>()
    private val checkedLists = mutableMapOf<NodeTabKey, List<Node>?>()
    private val currentLists = mutableMapOf<NodeTabKey, List<Node>?>()

    private val _storage = MutableStateFlow<List<NodeStorage>>(emptyList())
    private val _currentTab = MutableStateFlow(middleTab)
    private val _currentNode = MutableStateFlow<Node?>(null)
    private val _internalRoot = MutableStateFlow(Node.asRoot(internalStoragePath, NodeRootType.Storage(NodeStorage(NodeStorage.Kind.InternalStorage, internalStoragePath, "qwerty", "alias"))))
    private val _checked = MutableStateFlow<List<Node>>(listOf())
    private val _alerts = EventFlow<NodeError>()
    private val _removed = EventFlow<Node>()
    private val _deleted = EventFlow<List<Node>>()
    private val _updated = EventFlow<Node>()
    var currentItems = listOf<Node>()
        private set

    val currentTabKey: StateFlow<NodeTabKey> = _currentTab
    val currentNode: StateFlow<Node?> = _currentNode
    val storage: StateFlow<List<NodeStorage>> = _storage
    val internalStorage: StateFlow<Node> = _internalRoot
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
        _currentTab.value = tab
        updateChecked(tab)
        updateCurrentItems(tab)
        updateDeepest(tab)
    }

    fun updateInternalStorage(action: Node.() -> Node) {
        _internalRoot.run {
            value = value.action()
        }
    }

    fun setStorage(item: List<NodeStorage>) {
        _storage.value = item
    }

    suspend fun emitUpdate(item: Node) = _updated.emit(item)

    suspend fun emitRemoved(item: Node) = _removed.emit(item)

    suspend fun emitDeleted(items: List<Node>) = _deleted.emit(items)

    private fun updateChecked(tab: NodeTabKey? = _currentTab.value) {
        tab ?: return
        checkedLists.takeIf { tab == _currentTab.value }
            ?.let { it[tab] }
            ?.let { _checked.value = it }
    }

    private fun updateDeepest(tab: NodeTabKey? = _currentTab.value) {
        tab ?: return
        deepestNodes.takeIf { tab == _currentTab.value }
            ?.let { it[tab] }
            .let { _currentNode.value = it }
    }

    private fun updateCurrentItems(tab: NodeTabKey) {
        currentLists.takeIf { tab == _currentTab.value }
            ?.let { it[tab] }
            ?.let { currentItems = it }
    }
}