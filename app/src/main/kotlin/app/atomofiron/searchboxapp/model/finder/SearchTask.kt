package app.atomofiron.searchboxapp.model.finder

import java.util.UUID

typealias TextSearchTask = SearchTask<SearchResult.Text>
typealias FilesSearchTask = SearchTask<SearchResult.Files>
typealias GenericSearchTask = SearchTask<SearchResult>

data class SearchTask<Result : SearchResult>(
    val query: QueryParams,
    val result: Result,
    val uuid: UUID = UUID.randomUUID(),
    val state: SearchState = SearchState.Progress,
    val error: String? = null,
) {
    val uniqueId: Int get() = uuid.hashCode()
    val count: Int = result.count
    val withRetries: Boolean get() = false // todo remove

    val inProgress: Boolean get() = state == SearchState.Progress
    val isEnded: Boolean get() = state is SearchState.Ended
    val isStopped: Boolean get() = state is SearchState.Stopped
    val isError: Boolean get() = state is SearchState.Ended && error != null

    fun copyWith(result: Result): SearchTask<Result> = copy(result = result)

    fun toEnded(
        isStopped: Boolean = false,
        isRemovable: Boolean = true,
        result: Result = this.result,
        error: String? = this.error,
    ): SearchTask<Result> {
        val state = if (isStopped) SearchState.Stopped(isRemovable) else SearchState.Ended(isRemovable)
        return copy(state = state, result = result, error = error)
    }

    @Suppress("UNCHECKED_CAST")
    fun cast() = this as GenericSearchTask
}