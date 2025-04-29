package app.atomofiron.searchboxapp.custom.view.dock.shape

import android.graphics.Color

sealed interface DockStyle {
    val fill: Int
    val stroke: Int
    val strokeWidth: Float

    data class Fill(
        override val fill: Int,
    ) : DockStyle {
        override val stroke = Color.TRANSPARENT
        override val strokeWidth = 0f
    }

    data class Stroke(
        override val fill: Int,
        override val stroke: Int,
        override val strokeWidth: Float,
    ) : DockStyle
}