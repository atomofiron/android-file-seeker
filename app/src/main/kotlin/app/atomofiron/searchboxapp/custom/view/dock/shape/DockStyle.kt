package app.atomofiron.searchboxapp.custom.view.dock.shape

import android.graphics.Color

data class DockStyle(
    val fill: Int,
    val transparent: Boolean,
    val strokeWidth: Float,
) {
    companion object {
        val Stub = DockStyle(Color.TRANSPARENT, true, 0f)
    }
}