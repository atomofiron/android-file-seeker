package app.atomofiron.searchboxapp.custom.view.dock.item

import app.atomofiron.common.util.extension.hash

class DockItemChildren(
    val columns: Int,
    val secondary: Boolean = false,
    private val items: List<DockItem>,
) : List<DockItem> by items {
    companion object {
        val Empty = DockItemChildren()

        const val AUTO = 0
    }

    constructor(vararg items: DockItem, secondary: Boolean = false) : this(AUTO, secondary, items.toList())

    constructor(columns: Int, vararg items: DockItem, secondary: Boolean = false) : this(columns, secondary, items.toList())

    fun ids() = items.asIterable().map { it.id }

    fun copy(
        columns: Int = this.columns,
        secondary: Boolean = this.secondary,
        map: ((DockItem) -> DockItem)? = null,
    ): DockItemChildren {
        val items = map
            ?.let { items.map(it) }
            ?: items
        return DockItemChildren(columns, secondary, items)
    }

    override fun equals(other: Any?) = when {
        other !is DockItemChildren -> false
        other.columns != columns -> false
        other.secondary != secondary -> false
        other.items != items -> false
        else -> true
    }

    override fun hashCode(): Int = hash(columns, items)
}