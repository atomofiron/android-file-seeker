package app.atomofiron.searchboxapp.custom.view.dock.popup

import android.graphics.Rect
import app.atomofiron.searchboxapp.custom.view.dock.shape.DockStyle
import app.atomofiron.searchboxapp.model.Layout.Ground

data class DockPopupConfig(
    val ground: Ground,
    val rect: Rect,
    val style: DockStyle,
    val animated: Boolean,
) {
    companion object {
        val Stub = DockPopupConfig(Ground.Bottom, 0, 0, animated = false)
    }
    val spaceWidth get() = rect.width()
    val spaceHeight get() = rect.height()

    constructor(
        ground: Ground,
        spaceWidth: Int,
        spaceHeight: Int,
        animated: Boolean,
        style: DockStyle = DockStyle.Stub,
    ) : this(ground, Rect(0, 0, spaceWidth, spaceHeight), style, animated)
}