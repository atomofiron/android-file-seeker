package app.atomofiron.searchboxapp.model.textviewer

import app.atomofiron.searchboxapp.model.finder.SearchParams
import app.atomofiron.searchboxapp.model.finder.SearchResult
import java.util.*


sealed interface SearchState {
    data object Progress : SearchState
    data class Ended(val isRemovable: Boolean, val isStopped: Boolean) : SearchState
}

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
    val isStopped: Boolean get() = state is SearchState.Ended && state.isStopped
    val isError: Boolean get() = state is SearchState.Ended && error != null

    fun copyWith(result: SearchResult): SearchTask = copy(result = result)

    fun toEnded(
        isStopped: Boolean = false,
        isRemovable: Boolean = true,
        result: SearchResult = this.result,
        error: String? = this.error,
    ): SearchTask {
        return copy(
            state = SearchState.Ended(isRemovable, isStopped),
            result = result,
            error = error,
        )
    }

}