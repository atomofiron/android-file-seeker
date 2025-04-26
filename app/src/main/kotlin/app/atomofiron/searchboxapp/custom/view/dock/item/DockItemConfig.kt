package app.atomofiron.searchboxapp.custom.view.dock.item

import app.atomofiron.searchboxapp.custom.view.dock.popup.DockPopupConfig
import app.atomofiron.searchboxapp.model.Layout

data class DockItemConfig(
    val width: Int,
    val height: Int,
    val notch: Int,
    val popup: DockPopupConfig,
) {
    companion object {
        val Stub = DockItemConfig(0, 0, 0, DockPopupConfig.Stub)
    }
    val isZero get() = width <= 0 || height <= 0

    fun with(ground: Layout.Ground?): DockItemConfig = when (ground) {
        null, popup.ground -> this
        else -> copy(popup = popup.copy(ground = popup.ground))
    }
}
