package app.atomofiron.searchboxapp.screens.viewer.state

sealed interface CursorResult {
    /** to load more lines */
    data class Load(val line: Int) : CursorResult
    data class Err(val message: String) : CursorResult
    data object Ok : CursorResult
}
