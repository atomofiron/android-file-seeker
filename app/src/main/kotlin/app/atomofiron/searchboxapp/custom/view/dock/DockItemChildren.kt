package app.atomofiron.searchboxapp.custom.view.dock

data class DockItemChildren(
    val columns: Int,
    private val items: List<DockItem>,
) : List<DockItem> by items {
    companion object {
        val Stub = DockItemChildren()

        const val AUTO = 0
    }

    constructor(vararg items: DockItem) : this(AUTO, *items)

    constructor(columns: Int, vararg items: DockItem) : this(columns, items.toList())
}