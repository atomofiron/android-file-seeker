package app.atomofiron.searchboxapp.model.finder

import app.atomofiron.searchboxapp.model.explorer.NodeError
import java.util.UUID

typealias LocalSearchTask = SearchTask<LocalSearchResult>
typealias GlobalSearchTask = SearchTask<GlobalSearchResult>
typealias GenericSearchTask = SearchTask<SearchResult>

data class SearchTask<Result : SearchResult>(
    val query: QueryParams,
    val result: Result,
    val uuid: UUID = UUID.randomUUID(),
    val uniqueId: Int = uuid.hashCode(), // for restored from cache, todo move back to fields, replace UUID with Uuid and replace SearchResultCache.id with Uuid
    val status: SearchStatus = SearchStatus.Progress,
    val error: NodeError? = null,
    val cached: Boolean = false,
) {
    val count: Int = result.count

    val isRemovable get() = result.removable
    val isProgress: Boolean get() = status is SearchStatus.Progress
    val isStopping: Boolean get() = status is SearchStatus.Stopping
    val isEnded: Boolean get() = status is SearchStatus.Ended
    val isStopped: Boolean get() = status is SearchStatus.Ended && status.stopped
    val isError: Boolean get() = status is SearchStatus.Ended && error != null

    fun toEnded(
        result: Result = this.result,
        error: NodeError? = this.error,
        stopped: Boolean = false,
    ): SearchTask<Result> {
        val state = SearchStatus.Ended(stopped = stopped)
        return copy(status = state, result = result, error = error)
    }

    @Suppress("UNCHECKED_CAST")
    fun upcast() = this as GenericSearchTask
}