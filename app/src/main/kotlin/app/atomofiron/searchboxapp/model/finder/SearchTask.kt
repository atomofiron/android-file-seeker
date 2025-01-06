package app.atomofiron.searchboxapp.model.finder

import java.util.*

data class SearchTask(
    val uuid: UUID,
    val params: SearchParams,
    val result: SearchResult,
    val state: SearchState = SearchState.Progress,
    val error: String? = null,
) {
    val uniqueId: Int get() = uuid.hashCode()
    val count: Int = result.count

    val inProgress: Boolean get() = state == SearchState.Progress
    val isEnded: Boolean get() = state is SearchState.Ended
    val isStopped: Boolean get() = state is SearchState.Stopped
    val isError: Boolean get() = state is SearchState.Ended && error != null

    fun copyWith(result: SearchResult): SearchTask = copy(result = result)

    fun toEnded(
        isStopped: Boolean = false,
        isRemovable: Boolean = true,
        result: SearchResult = this.result,
        error: String? = this.error,
    ): SearchTask {
        return copy(
            state = if (isStopped) SearchState.Stopped(isRemovable) else SearchState.Ended(
                isRemovable
            ),
            result = result,
            error = error,
        )
    }
}