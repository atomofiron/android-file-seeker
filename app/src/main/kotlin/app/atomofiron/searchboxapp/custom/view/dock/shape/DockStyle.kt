package app.atomofiron.searchboxapp.custom.view.dock.shape

import android.graphics.Color

sealed interface DockStyle {
    val fill: Int
    val stroke: Int

    data class Fill(
        override val fill: Int,
    ) : DockStyle {
        override val stroke: Int = Color.TRANSPARENT
    }

    data class Stroke(
        override val fill: Int,
        override val stroke: Int,
    ) : DockStyle
}