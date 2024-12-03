package app.atomofiron.common.util

import java.util.Objects

class Unique<T>(val value: T) : Equality {
    companion object {
        private var nextId = 0
    }
    private val uniqueId = nextId++

    override fun hashCode(): Int = Objects.hash(this::class, uniqueId)

    override fun equals(other: Any?): Boolean = (other as? Unique<*>)?.uniqueId == uniqueId
}

interface Equality {
    override fun hashCode(): Int
    override fun equals(other: Any?): Boolean
}