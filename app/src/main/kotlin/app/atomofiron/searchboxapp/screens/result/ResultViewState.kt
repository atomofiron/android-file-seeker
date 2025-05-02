package app.atomofiron.searchboxapp.screens.result

import app.atomofiron.common.util.AlertMessage
import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.DeferredStateFlow
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.launch
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItemChildren
import app.atomofiron.searchboxapp.injectable.store.FinderStore
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.finder.SearchTask
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.model.toDockItem
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import app.atomofiron.searchboxapp.screens.result.state.ResultDockState
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.sortBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combineTransform

class ResultViewState(
    params: ResultPresenterParams,
    private val finderStore: FinderStore,
    private val scope: CoroutineScope,
) {
    private val taskId = params.taskId
    val task = DeferredStateFlow<SearchTask>()
    val composition = DeferredStateFlow<ExplorerItemComposition>()
    val alerts = ChannelFlow<AlertMessage.Res>()
    val checked = MutableStateFlow(listOf<Int>())
    val dock = MutableStateFlow(ResultDockState.Default) // todo pass default sorting

    init {
        transformState()
    }

    fun showAlert(message: AlertMessage.Res) {
        alerts[scope] = message
    }

    private fun transformState() {
        if (taskId != Const.UNDEFINED) combineTransform(finderStore.tasksFlow, checked) { tasks, checked ->
            val couple = tasks.task(checked)
            couple ?: return@combineTransform
            val (newTask, sorting) = couple
            task.value = newTask
            dock.value = dock(newTask, sorting)
            emit(Unit)
        }.launch(scope, Dispatchers.Default)
    }

    private fun List<SearchTask>.task(checked: List<Int>): Pair<SearchTask,NodeSorting>? {
        return find { it.uniqueId == taskId }?.let { task ->
            val result = task.result as SearchResult.FinderResult
            val matches = result.matches.map { match ->
                when {
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
            val newTask = task.copy(result = result.copy(matches = matches))
            newTask to sorting
        }
    }

    private fun dock(task: SearchTask, new: NodeSorting): ResultDockState {
        return dock.value.run {
            val sorting = when  {
                sorting.children.selectionMatches(new) -> sorting
                else -> new.toDockItem(sorting.label).copy(children = sorting.children.makeSelected(new))
            }
            copy(
                status = status.copy(clickable = task.inProgress),
                sorting = sorting,
                share = share.copy(enabled = !task.result.isEmpty),
                export = export.copy(enabled = !task.result.isEmpty),
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