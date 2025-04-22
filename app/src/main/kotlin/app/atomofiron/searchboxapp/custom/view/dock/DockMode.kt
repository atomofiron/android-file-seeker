package app.atomofiron.searchboxapp.custom.view.dock

import app.atomofiron.searchboxapp.model.Layout.Ground

sealed interface DockMode {
    val ground: Ground
    val isBottom get() = ground.isBottom
    data class Pinned(override val ground: Ground) : DockMode
    data class Popup(override val ground: Ground, val columns: Int) : DockMode
}
