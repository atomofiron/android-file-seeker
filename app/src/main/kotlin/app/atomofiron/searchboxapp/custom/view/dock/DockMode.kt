package app.atomofiron.searchboxapp.custom.view.dock

import app.atomofiron.searchboxapp.custom.view.dock.popup.DockPopupConfig
import app.atomofiron.searchboxapp.custom.view.dock.shape.DockNotch
import app.atomofiron.searchboxapp.model.Layout.Ground

sealed interface DockMode {
    val ground: Ground
    val isBottom get() = ground.isBottom

    data class Pinned(
        override val ground: Ground,
        val notch: DockNotch?,
    ) : DockMode

    data class Popup(val config: DockPopupConfig, val columns: Int) : DockMode {
        override val ground get() = config.ground
    }
}
