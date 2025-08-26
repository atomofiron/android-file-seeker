package app.atomofiron.searchboxapp.screens.finder.fragment.keyboard

sealed class GestureTracking(
    val consuming: Boolean,
    val vertical: Boolean,
) {
    data object None : GestureTracking(false, false)
    data object Vertical : GestureTracking(true, true)
    data class Horizontal(val direction: GestureDirection) : GestureTracking(true, false)
}

val GestureTracking?.consuming: Boolean get() = this?.consuming == true

val GestureTracking?.vertical: Boolean get() = this?.vertical == true
