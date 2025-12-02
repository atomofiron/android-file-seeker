package app.atomofiron.searchboxapp.model.other

sealed class AppState(
    val level: Int,
    val started: Boolean = false,
    val foreground: Boolean = false,
    open val rise: Boolean = false,
) {
    data object Unknown : AppState(level = 0, rise = false)

    data class Started(private val prev: AppState) : AppState(level = 1, started = true) {
        override val rise = prev.level < level
    }

    data object Foreground : AppState(level = 2, started = true, foreground = true, rise = true)
}