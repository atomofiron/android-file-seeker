package app.atomofiron.searchboxapp.screens.result

import app.atomofiron.common.util.AlertMessage
import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.launch
import app.atomofiron.common.util.flow.set
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemChildren
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.finder.SearchResult.Files
import app.atomofiron.searchboxapp.model.finder.GenericSearchTask
import app.atomofiron.searchboxapp.model.toDockItem
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import app.atomofiron.searchboxapp.screens.result.state.ResultDockState
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.sortBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combineTransform
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
    var error: String? = null
        private set
    val result = MutableStateFlow(Files.Stub)
    val composition = preferenceStore.explorerItemComposition
    val alerts = ChannelFlow<AlertMessage.Res>()
    val checked = MutableStateFlow(listOf<Int>())
    val dock = MutableStateFlow(ResultDockState.Default)

    init {
        transformState()
    }

    fun showAlert(message: AlertMessage.Res) {
        alerts[scope] = message
    }

    private fun transformState() {
        if (taskId != Const.UNDEFINED) combineTransform(finderStore.tasksFlow, checked) { tasks, checked ->
            emit(reduce(tasks, checked))
        }.launch(scope, Dispatchers.Default)
    }

    private fun reduce(tasks: List<GenericSearchTask>, checked: List<Int>) {
        tasks.find { it.uniqueId == taskId }?.let { task ->
            taskUuid = task.uuid
            error = task.error
            val result = task.result as Files
            val matches = result.matches.mapNotNull { match ->
                when {
                    mimeTypes.isNotEmpty() && !match.item.content.matchesAny(mimeTypes) -> null
                    !checked.contains(match.item.uniqueId) -> match
                    else -> match.update(match.item.copy(isChecked = true))
                }
            }.toMutableList()
            val sorting = result.sorting
            when (sorting) {
                is NodeSorting.Date -> matches.sortBy(sorting.reversed) { it.item.date }
                is NodeSorting.Name -> matches.sortBy(sorting.reversed) { it.item.name }
                is NodeSorting.Size -> matches.sortBy(sorting.reversed) { it.item.length }
            }
            matches.sortBy { !it.isDirectory }
            val newResult = result.copy(matches = matches)
            this.result.value = newResult
            dock.reduce(task.inProgress, newResult, sorting, checked = checked.size)
        }
    }

    private fun MutableStateFlow<ResultDockState>.reduce(
        inProgress: Boolean,
        result: Files,
        newSorting: NodeSorting,
        checked: Int,
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
                share = share.copy(enabled = result.matches.isNotEmpty()),
                export = export?.takeIf { mode.default }?.copy(enabled = result.matches.isNotEmpty()),
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
}