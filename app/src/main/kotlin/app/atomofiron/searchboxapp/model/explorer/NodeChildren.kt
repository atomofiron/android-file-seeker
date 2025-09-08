package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.extension.mutableCopy
import java.util.*

data class NodeChildren(
    // a copy is made during rendering by NodeChildren.fetch()
    val items: MutableList<Node>,
    // isOpened is always false in the garden
    val isOpened: Boolean = false,
) : List<Node> by items {

    private val names = items.map { it.name }.toMutableList()
    var dirs = items.count { it.isDirectory }
        private set

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

    inline fun update(updateMetadata: Boolean = true, action: MutableList<Node>.() -> Unit) {
        items.action()
        if (updateMetadata) updateMetadata()
    }

    fun updateMetadata() {
        dirs = items.count { it.isDirectory }
        names.clear()
        items.forEach { names.add(it.name) }
    }

    fun fetch(isOpened: Boolean = this.isOpened) = NodeChildren(isOpened = isOpened, items = items.mutableCopy())
}