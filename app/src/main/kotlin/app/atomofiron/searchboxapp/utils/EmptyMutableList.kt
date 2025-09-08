package app.atomofiron.searchboxapp.utils

class EmptyMutableList<T> : MutableList<T> by mutableListOf() {
    override fun add(element: T): Boolean = false
    override fun add(index: Int, element: T) = Unit
    override fun addAll(index: Int, elements: Collection<T>): Boolean = false
    override fun addAll(elements: Collection<T>): Boolean = false
    override fun addFirst(e: T) = Unit
    override fun addLast(e: T) = Unit
}
