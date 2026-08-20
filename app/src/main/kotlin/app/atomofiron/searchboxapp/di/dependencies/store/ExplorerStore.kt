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

    val storages: StateFlow<List<NodeStorage>>
        field = MutableStateFlow(emptyList())
    val currentTab: StateFlow<ExplorerTabKey>
        field = MutableStateFlow(middleTab)
    val currentDeepest: StateFlow<Node?>
        field = MutableStateFlow(null)
    val mainStorage: StateFlow<Node?>
        field = MutableStateFlow(null)
    val sorting: StateFlow<Map<NodeTabKey, NodeSorting>>
        field = MutableStateFlow(mapOf())
    val screenshots: StateFlow<NodeRef?>
        field = MutableStateFlow(null)
    val checked: StateFlow<List<Node>>
        field = MutableStateFlow(listOf())
    val alerts: Flow<Alert>
        field = EventFlow<Alert>()
    val deleted: Flow<List<Node>>
        field = EventFlow<List<Node>>()
    val copied: Flow<List<Node>>
        field = EventFlow<List<Node>>()
    val moved: Flow<List<Node>>
        field = EventFlow<List<Node>>()
    val updated: Flow<Node>
        field = EventFlow<Node>()
    val pasteBuffer: StateFlow<List<Node>>
        field = MutableStateFlow(emptyList())
    var currentItems = listOf<Node>()
        private set

    val currentTabKey: StateFlow<ExplorerTabKey> = currentTab
    val currentSorting: Flow<Pair<NodeTabKey, NodeSorting?>> = combine(sorting, currentTab) { sorting, key ->
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
        if (tab != currentTab.value) {
            currentTab.value = tab
            updateChecked(tab)
            updateCurrentItems(tab)
            updateDeepest(tab)
        }
    }

    fun resetCopyBuffer() = setForCopy(emptyList())

    fun setForCopy(list: List<Node>) {
        pasteBuffer.value = list
    }

    fun setMainStorage(item: NodeStorage?) {
        mainStorage.value = item?.let {
            NodeRef(it.path).toRoot(NodeRootInfo.Storage(it))
        }
    }

    fun updateScreenshots(ref: NodeRef) {
        screenshots.value = ref
    }

    fun setStorage(item: List<NodeStorage>) {
        storages.value = item
    }

    suspend fun emitUpdate(item: Node) = updated.emit(item)

    suspend fun emitDeleted(item: Node) = deleted.emit(listOf(item))

    suspend fun emitDeleted(items: List<Node>) = deleted.emit(items)

    suspend fun emitCopied(item: Node) = copied.emit(listOf(item))

    suspend fun emitCopied(items: List<Node>) = copied.emit(items)

    suspend fun emitMoved(item: Node) = moved.emit(listOf(item))

    suspend fun emitMoved(items: List<Node>) = moved.emit(items)

    suspend fun emitAlert(alert: Alert) = alerts.emit(alert)

    fun setSorting(key: NodeTabKey, sorting: NodeSorting?) {
        this.sorting.value = this.sorting.value.toMutableMap().apply {
            when (sorting) {
                null -> remove(key)
                else -> set(key, sorting)
            }
        }
    }

    private fun updateChecked(tab: NodeTabKey? = currentTab.value) {
        tab ?: return
        checkedLists.takeIf { tab == currentTab.value }
            ?.let { it[tab] }
            ?.let { checked.value = it }
    }

    private fun updateDeepest(tab: NodeTabKey? = currentTab.value) {
        tab ?: return
        deepestNodes.takeIf { tab == currentTab.value }
            ?.let { currentDeepest.value = it[tab] }
    }

    private fun updateCurrentItems(tab: NodeTabKey) {
        tab.takeIf { it == currentTab.value }
            ?.let { currentLists[it] }
            ?.let { currentItems = it }
    }
}