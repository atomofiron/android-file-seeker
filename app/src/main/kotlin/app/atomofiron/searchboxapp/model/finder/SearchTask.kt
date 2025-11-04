package app.atomofiron.searchboxapp.model.finder

import java.util.UUID

typealias TextSearchTask = SearchTask<SearchResult.Text>
typealias FilesSearchTask = SearchTask<SearchResult.Files>
typealias GenericSearchTask = SearchTask<SearchResult>

data class SearchTask<Result : SearchResult>(
    val query: QueryParams,
    val result: Result,
    val uuid: UUID = UUID.randomUUID(),
    val status: SearchStatus = SearchStatus.Progress,
    val error: String? = null,
) {
    val uniqueId: Int get() = uuid.hashCode()
    val count: Int = result.count

    val isProgress: Boolean get() = status is SearchStatus.Progress
    val isStopping: Boolean get() = status is SearchStatus.Stopping
    val isEnded: Boolean get() = status is SearchStatus.Ended
    val isStopped: Boolean get() = status is SearchStatus.Ended && status.stopped
    val isError: Boolean get() = status is SearchStatus.Ended && error != null

    fun copyWith(result: Result): SearchTask<Result> = copy(result = result)

    fun toEnded(
        result: Result = this.result,
        error: String? = this.error,
        stopped: Boolean = false,
        removable: Boolean = true,
    ): SearchTask<Result> {
        val state = SearchStatus.Ended(removable = removable, stopped = stopped)
        return copy(status = state, result = result, error = error)
    }

    @Suppress("UNCHECKED_CAST")
    fun upcast() = this as GenericSearchTask
}