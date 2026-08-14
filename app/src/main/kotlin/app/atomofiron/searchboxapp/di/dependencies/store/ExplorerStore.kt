package app.atomofiron.searchboxapp.di.dependencies.store

import app.atomofiron.common.util.Alert
import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.searchboxapp.model.explorer.ExplorerTabKey
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.NodeRootInfo
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.explorer.NodeStorage
import app.atomofiron.searchboxapp.model.explorer.NodeTabKey
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toRoot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExplorerStore @Inject constructor() {

    val firstTab = NodeTabKey.Explorer(0)
    val middleTab = NodeTabKey.Explorer(1)
    val lastTab = NodeTabKey.Explorer(2)
    val mainTabs = listOf(firstTab, middleTab, lastTab)


    private val deepestNodes = mutableMapOf<NodeTabKey, Node?>()
    private val checkedLists = mutableMapOf<NodeTabKey, List<Node>?>()
    private val currentLists = mutableMapOf<NodeTabKey, List<Node>?>()

    private val _storage = MutableStateFlow<List<NodeStorage>>(emptyList())
    private val _currentTab = MutableStateFlow(middleTab)
    private val _currentDeepest = MutableStateFlow<Node?>(null)
    private val _mainStorage = MutableStateFlow<Node?>(null)
    private val _sorting = MutableStateFlow<Map<NodeTabKey, NodeSorting>>(mapOf())
    private val _screenshots = MutableStateFlow<NodeRef?>(null)
    private val _checked = MutableStateFlow<List<Node>>(listOf())
    private val _alerts = EventFlow<Alert>()
    private val _deleted = EventFlow<List<Node>>()
    private val _copied = EventFlow<List<Node>>()
    private val _moved = EventFlow<List<Node>>()
    private val _updated = EventFlow<Node>()
    private val _pasteBuffer = MutableStateFlow<List<Node>>(emptyList())
    var currentItems = listOf<Node>()
        private set

    val currentTabKey: StateFlow<ExplorerTabKey> = _currentTab
    val currentDeepest: StateFlow<Node?> = _currentDeepest
    val storages: StateFlow<List<NodeStorage>> = _storage
    val mainStorage: StateFlow<Node?> = _mainStorage
    val screenshots: StateFlow<NodeRef?> = _screenshots
    val checked: StateFlow<List<Node>> = _checked
    val alerts: Flow<Alert> = _alerts
    val deleted: Flow<List<Node>> = _deleted
    val copied: Flow<List<Node>> = _copied
    val moved: Flow<List<Node>> = _moved
    val updated: Flow<Node> = _updated
    val pasteBuffer: StateFlow<List<Node>> = _pasteBuffer
    val sorting: StateFlow<Map<NodeTabKey, NodeSorting>> = _sorting
    val currentSorting: Flow<Pair<NodeTabKey, NodeSorting?>> = combine(_sorting, _currentTab) { sorting, key ->
        key to sorting[key]
    }

    fun setCurrentItems(tab: ExplorerTabKey, items: List<Node>) {
        currentLists[tab] = items
        updateCurrentItems(tab)
    }

    fun emitChecked(tab: ExplorerTabKey, items: List<Node>) {
        checkedLists[tab] = items
        updateChecked(tab)
    }

    fun setDeepest(tab: ExplorerTabKey, item: Node?) {
        deepestNodes[tab] = item
        updateDeepest(tab)
    }

    fun setCurrentTab(tab: ExplorerTabKey) {
        if (tab != _currentTab.value) {
            _currentTab.value = tab
            updateChecked(tab)
            updateCurrentItems(tab)
            updateDeepest(tab)
        }
    }

    fun resetCopyBuffer() = setForCopy(emptyList())

    fun setForCopy(list: List<Node>) {
        _pasteBuffer.value = list
    }

    fun setMainStorage(item: NodeStorage?) {
        _mainStorage.value = item?.let {
            NodeRef(it.path).toRoot(NodeRootInfo.Storage(it))
        }
    }

    fun updateScreenshots(ref: NodeRef) {
        _screenshots.value = ref
    }

    fun setStorage(item: List<NodeStorage>) {
        _storage.value = item
    }

    suspend fun emitUpdate(item: Node) = _updated.emit(item)

    suspend fun emitDeleted(item: Node) = _deleted.emit(listOf(item))

    suspend fun emitDeleted(items: List<Node>) = _deleted.emit(items)

    suspend fun emitCopied(item: Node) = _copied.emit(listOf(item))

    suspend fun emitCopied(items: List<Node>) = _copied.emit(items)

    suspend fun emitMoved(item: Node) = _moved.emit(listOf(item))

    suspend fun emitMoved(items: List<Node>) = _moved.emit(items)

    suspend fun emitAlert(alert: Alert) = _alerts.emit(alert)

    fun setSorting(key: NodeTabKey, sorting: NodeSorting?) {
        _sorting.value = _sorting.value.toMutableMap().apply {
            when (sorting) {
                null -> remove(key)
                else -> set(key, sorting)
            }
        }
    }

    private fun updateChecked(tab: NodeTabKey? = _currentTab.value) {
        tab ?: return
        checkedLists.takeIf { tab == _currentTab.value }
            ?.let { it[tab] }
            ?.let { _checked.value = it }
    }

    private fun updateDeepest(tab: NodeTabKey? = _currentTab.value) {
        tab ?: return
        deepestNodes.takeIf { tab == _currentTab.value }
            ?.let { _currentDeepest.value = it[tab] }
    }

    private fun updateCurrentItems(tab: NodeTabKey) {
        tab.takeIf { it == _currentTab.value }
            ?.let { currentLists[it] }
            ?.let { currentItems = it }
    }
}