package app.atomofiron.searchboxapp.model.finder

import app.atomofiron.searchboxapp.model.explorer.NodeError
import java.util.UUID

typealias TextSearchTask = SearchTask<SearchResult.Local>
typealias FilesSearchTask = SearchTask<SearchResult.Global>
typealias GenericSearchTask = SearchTask<SearchResult>

data class SearchTask<Result : SearchResult>(
    val query: QueryParams,
    val result: Result,
    val uuid: UUID = UUID.randomUUID(),
    val status: SearchStatus = SearchStatus.Progress,
    val error: NodeError? = null,
) {
    val uniqueId: Int get() = uuid.hashCode()
    val count: Int = result.count

    val isProgress: Boolean get() = status is SearchStatus.Progress
    val isStopping: Boolean get() = status is SearchStatus.Stopping
    val isEnded: Boolean get() = status is SearchStatus.Ended
    val isStopped: Boolean get() = status is SearchStatus.Ended && status.stopped
    val isError: Boolean get() = status is SearchStatus.Ended && error != null

    fun toEnded(
        result: Result = this.result,
        error: NodeError? = this.error,
        stopped: Boolean = false,
        removable: Boolean = true,
    ): SearchTask<Result> {
        val state = SearchStatus.Ended(removable = removable, stopped = stopped)
        return copy(status = state, result = result, error = error)
    }

    @Suppress("UNCHECKED_CAST")
    fun upcast() = this as GenericSearchTask
}