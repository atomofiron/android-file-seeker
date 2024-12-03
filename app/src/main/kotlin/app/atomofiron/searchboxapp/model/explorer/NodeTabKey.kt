package app.atomofiron.searchboxapp.model.explorer

import java.util.Objects

class NodeTabKey(
    private val id: String,
    val index: Int = -1,
) {

    private val hash = Objects.hash(this::class, id)

    override fun hashCode(): Int = hash

    override fun equals(other: Any?): Boolean = when {
        other !is NodeTabKey -> false
        other.id != id -> false
        else -> true
    }
}