package app.atomofiron.searchboxapp.model.finder

sealed class SearchState(
    open val running: Boolean,
    open val removable: Boolean,
    val order: Int,
) {
    data object Progress : SearchState(running = true, removable = false, order = 0)
    data object Stopping : SearchState(running = true, removable = false, order = 1)
    data class Stopped(override val removable: Boolean = true) : SearchState(running = false, removable = removable, order = 2)
    data class Ended(override val removable: Boolean = true) : SearchState(running = false, removable = removable, order = 3)
}
