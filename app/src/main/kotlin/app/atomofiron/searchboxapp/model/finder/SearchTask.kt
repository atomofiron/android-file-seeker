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

    val inProgress: Boolean get() = status is SearchStatus.Progress
    val isEnded: Boolean get() = status is SearchStatus.Ended
    val isStopped: Boolean get() = status is SearchStatus.Stopped
    val isError: Boolean get() = status is SearchStatus.Ended && error != null

    fun copyWith(result: Result): SearchTask<Result> = copy(result = result)

    fun toEnded(
        isStopped: Boolean = this.status is SearchStatus.Stopping,
        result: Result = this.result,
        error: String? = this.error,
    ): SearchTask<Result> {
        val state = when {
            isStopped -> SearchStatus.Stopped()
            else -> SearchStatus.Ended()
        }
        return copy(status = state, result = result, error = error)
    }

    @Suppress("UNCHECKED_CAST")
    fun upcast() = this as GenericSearchTask
}