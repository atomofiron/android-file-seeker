package app.atomofiron.searchboxapp.custom.view.dock

data class DockItemChildren(
    private val items: List<DockItem>,
    val columns: Int,
) : List<DockItem> by items {
    companion object {
        val Stub = DockItemChildren(emptyList(), columns = 0)

        const val AUTO = 0
    }
    constructor(vararg items: DockItem, columns: Int = AUTO) : this(items.toList(), columns)
}