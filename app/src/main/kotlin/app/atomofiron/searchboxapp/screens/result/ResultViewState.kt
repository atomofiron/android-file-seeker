package app.atomofiron.searchboxapp.screens.result

import app.atomofiron.common.util.Alert
import app.atomofiron.common.util.MutableList
import app.atomofiron.common.util.extension.launchOnDefault
import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.TriggerFlow
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.launch
import app.atomofiron.common.util.flow.set
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemChildren
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.explorer.NodeId
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.finder.GenericSearchTask
import app.atomofiron.searchboxapp.model.finder.SearchResult.Global
import app.atomofiron.searchboxapp.model.toDockItem
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItem
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import app.atomofiron.searchboxapp.screens.result.state.ResultDockState
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.ExplorerUtils.resolveContent
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toNode
import app.atomofiron.searchboxapp.utils.sortBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.mapNotNull
import java.util.UUID

class ResultViewState(
    params: ResultPresenterParams,
    val mode: ActivityMode,
    private val finderStore: FinderStore,
    private val scope: CoroutineScope,
    preferenceStore: PreferenceStore,
) {
    private val mimeTypes = mode.mimeFilters() ?: emptyList()
    private val taskId = params.taskId
    lateinit var taskUuid: UUID
        private set
    private var error: NodeError? = null

    private var _result: Global = Global.Stub
    val result: Global get() = _result
    private val _cache = mutableMapOf<NodeId, ResultItem.Item>()
    val cache: Map<NodeId, ResultItem.Item> = _cache
    private val _items = MutableStateFlow<List<ResultItem>>(emptyList())
    val items: StateFlow<List<ResultItem>> = _items
    private val _updates = ChannelFlow<ResultItem>()
    val updates: Flow<ResultItem> = _updates
    private val _checked = hashSetOf<NodeId>()
    val checked: Set<NodeId> = _checked
    private val _dock = MutableStateFlow(ResultDockState.Default)
    val dock: StateFlow<ResultDockState> = _dock

    private val checkedEvent = TriggerFlow(initial = emptySet<NodeId>())
    private val renderRequest = TriggerFlow(initial = Unit)
    val composition = preferenceStore.explorerItemComposition
    val alerts = ChannelFlow<Alert>()

    init {
        transformState()
    }

    fun showAlert(message: Alert.Uni) {
        alerts[scope] = message
    }

    private fun transformState() {
        if (taskId != Const.UNDEFINED) {
            val task = finderStore.tasksFlow.mapNotNull { tasks ->
                tasks.find { it.uniqueId == taskId }
            }
            combineTransform(renderRequest, task, checkedEvent) { _, task, checked ->
                emit(reduce(task, checked))
            }.launch(scope, Dispatchers.Default)
        }
    }

    private fun reduce(task: GenericSearchTask, checked: Set<Int>) {
        taskUuid = task.uuid
        if (error != task.error) {
            error = task.error
            task.error?.let {
                alerts[scope] = Alert(it)
            }
        }
        val result = task.result as Global
        result.matches.forEach {
            val cached = _cache[it.uniqueId]
            _cache[it.uniqueId] = when {
                cached == null -> {
                    val content = it.ref.resolveContent(it.meta.mime, it.meta.properties)
                    val item = it.ref.toNode(rootId = taskId, properties = it.meta.properties, content = content)
                    ResultItem.Item(match = it, item)
                }
                cached.match != it -> cached.copy(match = it)
                else -> return@forEach
            }
        }
        _result = result
        _dock.reduce(task.isProgress, result.sorting, checked = checked.size, hasMatches = result.matches.isNotEmpty())
        _items.renderItems(checked, result.sorting, result.errors.size)
    }

    private fun MutableStateFlow<ResultDockState>.reduce(
        inProgress: Boolean,
        newSorting: NodeSorting,
        checked: Int,
        hasMatches: Boolean,
    ) {
        value = value.run {
            val sorting = when {
                sorting.children.selectionMatches(newSorting) -> sorting
                else -> newSorting.toDockItem(sorting.id, sorting.label).copy(children = sorting.children.makeSelected(newSorting))
            }
            val status = if (status.clickable == inProgress) status else status.copy(
                clickable = inProgress,
                icon = DockItem.Icon(if (inProgress) R.drawable.ic_circle_stop else R.drawable.ic_circle_check),
                label = DockItem.Label(if (inProgress) R.string.stop else R.string.completed),
            )
            copy(
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
    }

    private fun DockItemChildren.makeSelected(sorting: NodeSorting): DockItemChildren {
        return copy(
            items = map {
                if (it.selectionMatches(sorting)) it else it.copy(selected = it.id == sorting)
            }
        )
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
        when (sorting) {
            is NodeSorting.Date -> items.sortBy(sorting.reversed) { it.item.date }
            is NodeSorting.Name -> items.sortBy(sorting.reversed) { it.item.name }
            is NodeSorting.Size -> items.sortBy(sorting.reversed) { it.item.length }
        }
        items.sortBy { !it.isDirectory }
        value = buildList(items.size.inc()) {
            val dirCount = items.count { it.isDirectory }
            val fileCount = items.size - dirCount
            add(ResultItem.Header(dirCount, fileCount, errors))
            addAll(items)
        }
    }

    suspend fun cache(item: ResultItem.Item) {
        val wasCached = _cache[item.uniqueId]?.isCached == true
        _cache[item.uniqueId] = item
        when {
            wasCached -> _updates.send(item)
            else -> renderRequest()
        }
    }

    fun setChecked(uniqueId: NodeId, toChecked: Boolean) {
        when {
            toChecked -> _checked.add(uniqueId)
            else -> _checked.remove(uniqueId)
        }
        scope.launchOnDefault {
            checkedEvent.emit(_checked.toSet())
        }
    }
}