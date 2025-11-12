package app.atomofiron.common.util

import java.util.Spliterator
import java.util.Spliterators

class GrowingList<E> private constructor(
    private val mutable: MutableList<E>,
) : List<E> by mutable {

    constructor() : this(mutableListOf())

    fun add(element: E): Boolean = mutable.add(element)

    fun add(index: Int, element: E) = mutable.add(index, element)

    fun addLast(element: E) = mutable.add(element)

    fun addAll(elements: Collection<E>): Boolean = mutable.addAll(elements)

    fun addAll(index: Int, elements: Collection<E>): Boolean = mutable.addAll(index, elements)

    fun fetch(): List<E> = FrozenList(this)
}

private class FrozenList<E>(private val source: List<E>) : List<E> {

    override val size = source.size

    override fun isEmpty(): Boolean = size == 0

    override fun get(index: Int): E {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("index: $index, size: $size")
        }
        return source[index]
    }

    override fun indexOf(element: E): Int {
        val index = source.indexOf(element)
        return when {
            index >= size -> -1
            else -> index
        }
    }

    override fun contains(element: E): Boolean = indexOf(element) >= 0

    override fun containsAll(elements: Collection<E>): Boolean = elements.all { contains(it) }

    override fun lastIndexOf(element: E): Int = indexOfLast { it == element }

    override fun spliterator(): Spliterator<E?> = Spliterators.spliterator(this, Spliterator.ORDERED)

    override fun iterator(): Iterator<E> = listIterator(0)

    override fun listIterator(): ListIterator<E> = listIterator(0)

    override fun listIterator(index: Int): ListIterator<E> = object : ListIterator<E> {
        private var current = index
        override fun hasNext() = current < size
        override fun next() = get(current++)
        override fun hasPrevious() = current > 0
        override fun previous() = get(--current)
        override fun nextIndex() = current
        override fun previousIndex() = current.dec()
    }

    override fun subList(fromIndex: Int, toIndex: Int): List<E> {
        subListRangeCheck(fromIndex, toIndex, size)
        return source.subList(fromIndex, toIndex)
    }

    private fun subListRangeCheck(fromIndex: Int, toIndex: Int, size: Int) = when {
        fromIndex < 0 -> throw IndexOutOfBoundsException("fromIndex = $fromIndex")
        toIndex > size -> throw IndexOutOfBoundsException("toIndex = $toIndex")
        else -> require(fromIndex <= toIndex) { "fromIndex($fromIndex) > toIndex($toIndex)" }
    }
}
