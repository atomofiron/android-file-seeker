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
import app.atomofiron.searchboxapp.model.finder.SearchStatus
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
import javax.inject.Inject
import kotlin.uuid.Uuid

@ResultScope
class ResultScreenState @Inject constructor(
    override val mode: ActivityMode,
    task: GlobalSearchTask?,
    private val scope: CoroutineScope,
    preferenceStore: PreferenceStore,
) : ResultViewState {

    private val mimeTypes = mode.mimeFilters() ?: emptyList()
    val taskUuid: Uuid = task?.uuid ?: Uuid.random()

    private var error: NodeError? = null
    private val mutex = Mutex()

    var result: GlobalSearchResult = task?.result ?: GlobalSearchResult(forText = false)
        private set
    override val isReady: StateFlow<Boolean>
        field = MutableStateFlow(task?.result?.matches?.isEmpty() == true)
    val cache: Map<NodeId, ResultItem.Item>
        field = mutableMapOf()
    override val items: StateFlow<List<ResultItem>>
        field = MutableStateFlow(emptyList())
    override val updates: Flow<ResultItem>
        field = EventFlow<ResultItem>()
    val checked: StateFlow<Set<NodeId>>
        field = MutableStateFlow(emptySet())
    override val dock: StateFlow<ResultDockState>
        field = MutableStateFlow(ResultDockState.Default.reduce(
            taskStatus = task?.status ?: SearchStatus.Ended(),
            newSorting = task?.sorting ?: NodeSorting.Name,
            checked = checked.value.size,
            hasMatches = result.matches.isNotEmpty()
        ))

    override val composition = preferenceStore.explorerItemComposition
    override val alerts = EventFlow<Alert>()

    fun showAlert(message: Alert) {
        alerts[scope] = message
    }

    fun reduce(task: GlobalSearchTask, checked: Set<Int>) {
        if (error != task.error) {
            error = task.error
            task.error?.let {
                alerts[scope] = it.toAlert()
            }
        }
        if (result.matches.isEmpty() && task.result.matches.isNotEmpty()) {
            isReady.value = false
        }
        result = task.result
        dock.value = dock.value.reduce(task.status, task.sorting, checked = checked.size, hasMatches = task.result.matches.isNotEmpty())
        items.renderItems(checked, task.sorting, task.result.errors.size)
    }

    private fun ResultDockState.reduce(
        taskStatus: SearchStatus,
        newSorting: NodeSorting,
        checked: Int,
        hasMatches: Boolean,
    ): ResultDockState {
        val sorting = when {
            sorting.children.selectionMatches(newSorting) -> sorting
            else -> newSorting.toDockItem(sorting.id, sorting.label).copy(children = sorting.children.makeSelected(newSorting))
        }
        val status = status.copy(
            clickable = taskStatus is SearchStatus.Progress,
            icon = when (taskStatus) {
                is SearchStatus.Progress -> DockItem.Icon(R.drawable.ic_circle_stop)
                is SearchStatus.Ended -> DockItem.Icon(R.drawable.ic_circle_check)
                is SearchStatus.Stopping -> null
            },
            label = DockItem.Label(
                when (taskStatus) {
                    is SearchStatus.Progress -> R.string.stop
                    is SearchStatus.Ended -> if (taskStatus.stopped) R.string.stopped else R.string.completed
                    is SearchStatus.Stopping -> R.string.stopping
                }
            ),
            progress = taskStatus is SearchStatus.Stopping,
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
        isReady.value = true
    }

    suspend fun cache(item: ResultItem.Item) {
        mutex.withLock {
            cache[item.uniqueId] = item
        }
        updates.emit(item)
    }

    suspend fun cache(items: List<ResultItem.Item>) {
        mutex.withLock {
            items.forEach {
                cache[it.uniqueId] = it
            }
        }
        items.forEach {
            updates.emit(it)
        }
    }

    fun setChecked(uniqueId: NodeId, toChecked: Boolean) {
        checked.update {
            it.toMutableSet().apply {
                when {
                    toChecked -> add(uniqueId)
                    else -> remove(uniqueId)
                }
            }
        }
    }
}