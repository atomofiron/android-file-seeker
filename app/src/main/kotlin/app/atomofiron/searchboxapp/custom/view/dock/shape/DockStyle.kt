package app.atomofiron.searchboxapp.custom.view.dock.shape

import android.graphics.Color

data class DockStyle(
    val fill: Int,
    val translucent: Boolean,
) {
    companion object {
        val Stub = DockStyle(Color.TRANSPARENT, true)
    }
}