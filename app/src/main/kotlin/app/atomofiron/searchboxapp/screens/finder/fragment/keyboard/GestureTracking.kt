package app.atomofiron.searchboxapp.screens.finder.fragment.keyboard

sealed interface GestureTracking {
    val unknown: Boolean get() = this == Unknown
    val skipped: Boolean get() = this == Skipping
    val cancelled: Boolean get() = this == Cancelled
    val vertical: Boolean get() = this == Vertical
    val horizontal: Boolean get() = this is Horizontal
    val any: Boolean get() = vertical || horizontal

    data object Unknown : GestureTracking
    data object Skipping : GestureTracking
    data object Cancelled : GestureTracking
    data object Vertical : GestureTracking
    data class Horizontal(val direction: GestureDirection) : GestureTracking
}