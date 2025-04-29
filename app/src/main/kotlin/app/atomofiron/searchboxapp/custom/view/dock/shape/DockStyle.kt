package app.atomofiron.searchboxapp.custom.view.dock.shape

sealed interface DockStyle {
    val color: Int

    data class Fill(override val color: Int) : DockStyle
    data class Stroke(override val color: Int) : DockStyle
}