package app.atomofiron.searchboxapp.model.explorer

import java.util.*

data class NodeChildren(
    // a copy is made during rendering by NodeChildren.fetch()
    val items: MutableList<Node>,
    // can be true in the tree and in the NodeRoot
    val isOpened: Boolean,
) : List<Node> by items {

    val names = items.map { it.name }.toMutableList()

    override fun hashCode(): Int = Objects.hash(isOpened, items.map { it.path })

    override fun equals(other: Any?): Boolean {
        return when {
            other !is NodeChildren -> false
            other.isOpened != isOpened -> false
            other.items.size != items.size -> false
            other.names.containsAll(names) -> false
            names.containsAll(other.names) -> false
            // do not compare the children because of ConcurrentModificationException
            else -> true
        }
    }

    inline fun update(updateNames: Boolean = true, action: MutableList<Node>.() -> Unit) {
        items.action()
        if (updateNames) updateChildrenNames()
    }

    fun updateChildrenNames() {
        names.clear()
        items.forEach { names.add(it.name) }
    }

    fun fetch(isOpened: Boolean = this.isOpened) = NodeChildren(isOpened = isOpened, items = items.toMutableList())
}