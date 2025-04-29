package app.atomofiron.searchboxapp.custom.view.dock.shape

data class DockNotch(
    val width: Float,
    val height: Float,
) {
    val radius = width / 2

    constructor(size: Int) : this(size.toFloat(), size.toFloat())
}
