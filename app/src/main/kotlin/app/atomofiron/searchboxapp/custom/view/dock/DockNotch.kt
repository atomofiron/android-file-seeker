package app.atomofiron.searchboxapp.custom.view.dock

data class DockNotch(
    val bias: Float,
    val width: Float,
    val height: Float,
) {
    val radius = width / 2
}
