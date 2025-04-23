package app.atomofiron.searchboxapp.custom.view.dock.popup

import android.graphics.Rect
import app.atomofiron.searchboxapp.model.Layout.Ground

data class DockPopupConfig(
    val ground: Ground,
    val rect: Rect,
) {
    companion object {
        val Stub = DockPopupConfig(Ground.Bottom, Rect(0, 0, 0, 0))
    }
    val spaceWidth get() = rect.width()
    val spaceHeight get() = rect.height()

    constructor(ground: Ground, spaceWidth: Int, spaceHeight: Int) : this(ground, Rect(0, 0, spaceWidth, spaceHeight))
}