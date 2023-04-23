package app.atomofiron.searchboxapp.model.finder

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch
import app.atomofiron.searchboxapp.utils.Const
import java.util.*


sealed class SearchResult {

    abstract val count: Int
    abstract val countTotal: Int

    val isEmpty: Boolean get() = count == 0

    abstract fun getCounters(): IntArray

    data class TextSearchResult(
        override val count: Int,
        /** line index -> matches byteOffset+length */
        val matchesMap: Map<Int, List<TextLineMatch>>,
        val indexes: List<Int>,
    ) : SearchResult() {

        override val countTotal = 1

        constructor() : this(0, mapOf(), listOf())

        override fun getCounters(): IntArray = intArrayOf(count)
    }

    data class FinderResult(
        private val forContent: Boolean,
        override val count: Int = 0,
        val matches: List<ItemMatch> = listOf(),
        override val countTotal: Int = 0,
    ) : SearchResult() {

        override fun getCounters(): IntArray = when {
            forContent -> intArrayOf(count, matches.size, countTotal)
            else -> intArrayOf(matches.size, countTotal)
        }

        fun toMarkdown(): String {
            val data = StringBuilder()
            for (item in matches) {
                val name = if (item.item.isDirectory) item.item.name + Const.SLASH else item.item.name
                val line = String.format("[%s](%s)\n", name, item.item.path.replace(" ", "\\ "))
                data.append(line)
            }
            return data.toString()
        }

        fun removeItem(item: Node): SearchResult {
            val index = matches.indexOfFirst { it.item.uniqueId == item.uniqueId }
            if (index < 0) return this
            val items = matches.toMutableList()
            val removed = items.removeAt(index)
            val count = count - removed.count
            return FinderResult(forContent, count, items, countTotal.dec())
        }

        fun add(itemCounter: ItemMatch): FinderResult {
            val items = matches.toMutableList()
            val count = count + itemCounter.count
            items.add(itemCounter)
            return FinderResult(forContent, count, items, countTotal.inc())
        }
    }

    override fun hashCode(): Int = Objects.hash(this::class, count, countTotal)

    override fun equals(other: Any?): Boolean = when {
        other === this -> true
        other !is SearchResult -> false
        other.count != count -> false
        other.countTotal != countTotal -> false
        else -> other::class == this::class
    }
}

fun SearchResult.TextSearchResult.toItemMatchMultiply(item: Node): ItemMatch {
    return ItemMatch.Multiply(item, count, matchesMap, indexes)
}

@Suppress("LeakingThis")
sealed class ItemMatch {

    abstract val item: Node
    abstract val count: Int

    val path get() = item.path
    val isDirectory: Boolean get() = item.isDirectory
    val isCached: Boolean get() = item.isCached
    val isDeleting: Boolean get() = item.state.isDeleting
    val isChecked: Boolean get() = item.isChecked
    val withCounter: Boolean get() = this !is Single

    abstract fun update(item: Node): ItemMatch

    data class Single(override val item: Node) : ItemMatch() {
        override val count = 1

        override fun update(item: Node): ItemMatch = copy(item = item)
    }

    data class Multiply(
        override val item: Node,
        override val count: Int,
        /** line index -> matches byteOffset+length */
        val matchesMap: Map<Int, List<TextLineMatch>>,
        val indexes: List<Int>,
    ) : ItemMatch() {
        override fun update(item: Node): ItemMatch = copy(item = item)
    }

    data class MultiplyError(
        override val item: Node,
        override val count: Int,
        val error: String,
    ) : ItemMatch() {
        override fun update(item: Node): ItemMatch = copy(item = item)
    }
}
