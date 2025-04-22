package app.atomofiron.searchboxapp.custom.view.dock.popup

import app.atomofiron.searchboxapp.model.Layout.Ground

data class DockPopupConfig(
    val spaceWidth: Int,
    val spaceHeight: Int,
    val ground: Ground,
) {
    companion object {
        val Stub = DockPopupConfig(0, 0, Ground.Bottom)
    }
}