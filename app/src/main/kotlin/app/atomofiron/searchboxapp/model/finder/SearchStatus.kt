package app.atomofiron.searchboxapp.model.finder

sealed class SearchStatus(
    open val running: Boolean,
    open val removable: Boolean,
    val order: Int,
) {
    data object Progress : SearchStatus(running = true, removable = false, order = 0)

    data object Stopping : SearchStatus(running = true, removable = false, order = 1)

    data class Ended(
        override val removable: Boolean = true,
        val stopped: Boolean = false,
    ) : SearchStatus(running = false, removable = removable, order = if (stopped) 2 else 3)
}
