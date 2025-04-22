package app.atomofiron.searchboxapp.custom.view.dock

import app.atomofiron.searchboxapp.model.Layout

data class DockItemConfig(
    val width: Int,
    val height: Int,
    val ground: Layout.Ground,
) {
    companion object {
        val Stub = DockItemConfig(0, 0, Layout.Ground.Bottom)
    }
    val isZero get() = width <= 0 || height <= 0
    val isBottom get() = ground.isBottom
}
