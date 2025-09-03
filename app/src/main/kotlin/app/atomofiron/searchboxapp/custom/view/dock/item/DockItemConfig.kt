package app.atomofiron.searchboxapp.custom.view.dock.item

import androidx.core.graphics.Insets
import app.atomofiron.searchboxapp.custom.view.dock.popup.DockPopupConfig
import app.atomofiron.searchboxapp.model.Layout

data class DockItemConfig(
    val width: Int,
    val height: Int,
    val colors: DockItemColors,
    val insets: Insets,
    val popup: DockPopupConfig,
) {
    companion object {
        val Stub = DockItemConfig(0, 0, DockItemColors(0, 0, 0, 0, 0), Insets.NONE, DockPopupConfig.Stub)
    }
    val isZero get() = width <= 0 || height <= 0

    fun with(ground: Layout.Ground?): DockItemConfig = when (ground) {
        null, popup.ground -> this
        else -> copy(popup = popup.copy(ground = popup.ground))
    }
}
