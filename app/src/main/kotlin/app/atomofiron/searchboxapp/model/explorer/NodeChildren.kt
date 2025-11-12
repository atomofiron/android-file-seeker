package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.extension.mutableCopy
import java.util.Objects

data class NodeChildren(
    // a copy is made during rendering by NodeChildren.fetch()
    val items: MutableList<Node>,
    // isOpened is always false in the garden
    val isOpened: Boolean = false,
    // hidden is always 0 in the garden
    val filteredOut: Int? = null,
) : List<Node> by items {

    private val names = items.map { it.name }.toMutableList()
    var dirs = items.count { it.isDirectory }
        private set

    override fun hashCode(): Int = Objects.hash(isOpened, items.map { it.ref })

    override fun equals(other: Any?): Boolean = when {
        other !is NodeChildren -> false
        other.isOpened != isOpened -> false
        other.items.size != items.size -> false
        other.names.containsAll(names) -> false
        names.containsAll(other.names) -> false
        // do not compare the children because of ConcurrentModificationException
        else -> true
    }

    override fun toString() = "NodeChildren(items=[${items.size}], isOpened=$isOpened, filteredOut=$filteredOut)"

    inline fun update(updateMetadata: Boolean = true, action: MutableList<Node>.() -> Unit) {
        items.action()
        if (updateMetadata) updateMetadata()
    }

    fun updateMetadata() {
        dirs = items.count { it.isDirectory }
        names.clear()
        items.forEach { names.add(it.name) }
    }

    fun fetch(isOpened: Boolean = this.isOpened) = NodeChildren(isOpened = isOpened, items = items.mutableCopy(), filteredOut = filteredOut)
}