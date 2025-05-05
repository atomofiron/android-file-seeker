package app.atomofiron.searchboxapp.custom.view.dock.shape

import android.graphics.Color

sealed interface DockStyle {
    val fill: Int
    val transparent: Boolean
    val strokeWidth: Float

    data class Fill(
        override val fill: Int,
        override val transparent: Boolean,
    ) : DockStyle {
        override val strokeWidth = 0f
    }

    data class Stroke(
        override val fill: Int,
        override val transparent: Boolean,
        override val strokeWidth: Float,
    ) : DockStyle

    companion object {
        val Stub = Fill(Color.TRANSPARENT, false)
    }
}