package app.atomofiron.searchboxapp.model.explorer

import java.util.*

data class NodeChildren(
    // список мутабельный, но перед публикацией делается копия через NodeChildren.copy()
    val items: MutableList<Node>,
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

    fun copy(items: MutableList<Node> = this.items): NodeChildren {
        return copy(isOpened = isOpened, items = items.toMutableList())
    }
}