package app.atomofiron.searchboxapp.model.finder

sealed class SearchStatus(
    open val running: Boolean = false,
) {
    data object Progress : SearchStatus(running = true)

    data object Stopping : SearchStatus(running = true)

    data class Ended(val stopped: Boolean = false) : SearchStatus()
}
