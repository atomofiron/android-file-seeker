package app.atomofiron.searchboxapp.model.finder

import app.atomofiron.common.util.TaskId
import app.atomofiron.searchboxapp.model.explorer.NodeError
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import kotlin.uuid.Uuid

typealias LocalSearchTask = SearchTask<LocalSearchResult>
typealias GlobalSearchTask = SearchTask<GlobalSearchResult>
typealias GenericSearchTask = SearchTask<SearchResult>

data class SearchTask<Result : SearchResult>(
    val query: QueryParams,
    val result: Result,
    val uuid: Uuid = Uuid.random(),
    val uniqueId: TaskId,
    val status: SearchStatus = SearchStatus.Progress,
    val cached: Boolean = false,
    val sorting: NodeSorting = NodeSorting.Date,
) {
    val count: Int = result.count

    val isRemovable get() = result.removable
    val isProgress: Boolean get() = status is SearchStatus.Progress
    val isStopping: Boolean get() = status is SearchStatus.Stopping
    val isEnded: Boolean get() = status is SearchStatus.Ended
    val isStopped: Boolean get() = status is SearchStatus.Ended && status.stopped
    val isError: Boolean get() = status is SearchStatus.Ended && error != null
    val error: NodeError? get() = result.error

    fun toEnded(
        result: Result = this.result,
        stopped: Boolean = false,
    ): SearchTask<Result> {
        val state = SearchStatus.Ended(stopped = stopped)
        return copy(status = state, result = result)
    }

    @Suppress("UNCHECKED_CAST")
    fun upcast() = this as GenericSearchTask
}