package app.atomofiron.searchboxapp.screens.result

import app.atomofiron.common.util.Alert
import app.atomofiron.common.util.MutableList
import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemChildren
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.explorer.NodeId
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.finder.GlobalSearchResult
import app.atomofiron.searchboxapp.model.finder.GlobalSearchTask
import app.atomofiron.searchboxapp.model.toDockItem
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItem
import app.atomofiron.searchboxapp.screens.result.state.ResultDockState
import app.atomofiron.searchboxapp.utils.ExplorerUtils.sortBy
import app.atomofiron.searchboxapp.utils.toAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject

@ResultScope
class ResultScreenState @Inject constructor(
    override val mode: ActivityMode,
    task: GlobalSearchTask?,
    private val scope: CoroutineScope,
    preferenceStore: PreferenceStore,
) : ResultViewStateTmp {

    private val mimeTypes = mode.mimeFilters() ?: emptyList()
    val taskUuid: UUID = task?.uuid ?: UUID.randomUUID()

    private var error: NodeError? = null
    private val mutex = Mutex()

    private var _result: GlobalSearchResult = task?.result ?: GlobalSearchResult(forText = false)
    val result: GlobalSearchResult get() = _result
    private val _cache = mutableMapOf<NodeId, ResultItem.Item>()
    val cache: Map<NodeId, ResultItem.Item> = _cache
    private val _items = MutableStateFlow<List<ResultItem>>(emptyList())
    override val items: StateFlow<List<ResultItem>> = _items
    private val _updates = EventFlow<ResultItem>()
    override val updates: Flow<ResultItem> = _updates
    private val _checked = MutableStateFlow<Set<NodeId>>(emptySet())
    val checked: StateFlow<Set<NodeId>> = _checked
    private val _dock = MutableStateFlow(ResultDockState.Default.reduce(inProgress = false, task?.sorting ?: NodeSorting.Name, checked = checked.value.size, hasMatches = result.matches.isNotEmpty()))
    override val dock: StateFlow<ResultDockState> = _dock

    override val composition = preferenceStore.explorerItemComposition
    override val alerts = EventFlow<Alert>()

    fun showAlert(message: Alert.Uni) {
        alerts[scope] = message
    }

    fun reduce(task: GlobalSearchTask, checked: Set<Int>) {
        if (error != task.error) {
            error = task.error
            task.error?.let {
                alerts[scope] = it.toAlert()
            }
        }
        _result = result
        _dock.value = _dock.value.reduce(task.isProgress, task.sorting, checked = checked.size, hasMatches = result.matches.isNotEmpty())
        _items.renderItems(checked, task.sorting, result.errors.size)
    }

    private fun ResultDockState.reduce(
        inProgress: Boolean,
        newSorting: NodeSorting,
        checked: Int,
        hasMatches: Boolean,
    ): ResultDockState {
        val sorting = when {
            sorting.children.selectionMatches(newSorting) -> sorting
            else -> newSorting.toDockItem(sorting.id, sorting.label).copy(children = sorting.children.makeSelected(newSorting))
        }
        val status = if (status.clickable == inProgress) status else status.copy(
            clickable = inProgress,
            icon = DockItem.Icon(if (inProgress) R.drawable.ic_circle_stop else R.drawable.ic_circle_check),
            label = DockItem.Label(if (inProgress) R.string.stop else R.string.completed),
        )
        return copy(
            status = status,
            sorting = sorting,
            share = share.copy(enabled = hasMatches),
            export = export?.takeIf { mode.default }?.copy(enabled = hasMatches),
            confirm = when (mode) {
                is ActivityMode.Default -> null
                is ActivityMode.Receive -> confirm?.copy(enabled = checked == 1)
                is ActivityMode.Share -> confirm?.copy(enabled = checked > 0 && (mode.multiple || checked == 1))
            },
        )
    }

    private fun DockItemChildren.makeSelected(sorting: NodeSorting): DockItemChildren {
        return copy {
            if (it.selectionMatches(sorting)) it else it.copy(selected = it.id == sorting)
        }
    }

    private fun DockItemChildren.selectionMatches(sorting: NodeSorting): Boolean {
        return all { it.selectionMatches(sorting) }
    }

    private fun DockItem.selectionMatches(sorting: NodeSorting): Boolean {
        return selected == (id == sorting)
    }

    private fun MutableStateFlow<List<ResultItem>>.renderItems(
        checked: Set<Int>,
        sorting: NodeSorting,
        errors: Int,
    ) {
        val items = MutableList<ResultItem.Item>(cache.size)
        cache.values.mapNotNullTo(items) { item ->
            when {
                mimeTypes.isNotEmpty() && !item.item.content.matchesAny(mimeTypes) -> null
                !checked.contains(item.uniqueId) -> item
                else -> item.copy(item = item.item.copy(isChecked = true))
            }
        }
        items.sortBy(sorting) { it.item }
        value = buildList(items.size.inc()) {
            val dirCount = items.count { it.isDirectory }
            val fileCount = items.size - dirCount
            add(ResultItem.Header(dirCount, fileCount, errors))
            addAll(items)
        }
    }

    suspend fun cache(item: ResultItem.Item) {
        mutex.withLock {
            _cache[item.uniqueId] = item
        }
        _updates.emit(item)
    }

    suspend fun cache(items: List<ResultItem.Item>) {
        mutex.withLock {
            items.forEach {
                _cache[it.uniqueId] = it
            }
        }
        items.forEach {
            _updates.emit(it)
        }
    }

    fun setChecked(uniqueId: NodeId, toChecked: Boolean) {
        _checked.update {
            it.toMutableSet().apply {
                when {
                    toChecked -> add(uniqueId)
                    else -> remove(uniqueId)
                }
            }
        }
    }
}