package app.atomofiron.searchboxapp.screens.finder.fragment.keyboard

sealed interface GestureTracking {
    data object None : GestureTracking
    data object Vertical : GestureTracking
    data class Horizontal(val direction: GestureDirection) : GestureTracking
}

val GestureTracking?.consuming: Boolean get() = when (this) {
    null, GestureTracking.None -> false
    GestureTracking.Vertical,
    is GestureTracking.Horizontal -> true
}
