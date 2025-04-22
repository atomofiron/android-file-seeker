package app.atomofiron.searchboxapp.custom.view.dock

sealed interface DockMode {
    data object Bottom : DockMode
    data object Flank : DockMode
    data class Children(
        val width: Int,
        val height: Int,
        val columns: Int,
    ) : DockMode
}
