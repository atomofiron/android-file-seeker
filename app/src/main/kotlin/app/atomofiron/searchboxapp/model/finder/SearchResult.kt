package app.atomofiron.searchboxapp.model.finder

import app.atomofiron.common.util.extension.hash
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeHash
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.textviewer.MatchMap
import java.util.Objects
import kotlin.LazyThreadSafetyMode.NONE

sealed class SearchResult {

    abstract val count: Int
    abstract val countTotal: Int

    val isEmpty: Boolean get() = count == 0

    abstract fun getCounters(): IntArray

    data class Local(
        override val count: Int,
        val matches: MatchMap,
        val hash: NodeHash? = null,
    ) : SearchResult() {

        val indexes: List<Int> by lazy(NONE) { matches.keys.sorted() }

        override val countTotal = 1

        constructor() : this(0, mapOf())

        override fun getCounters(): IntArray = intArrayOf(count)

        override fun hashCode(): Int = Objects.hash(this::class, count)

        override fun equals(other: Any?): Boolean = when {
            other === this -> true
            other !is Local -> false
            other.count != count -> false
            else -> other::class == this::class
        }
    }

    data class Global(
        private val forText: Boolean,
        override val count: Int = 0,
        override val countTotal: Int = 0,
        val matches: List<ItemMatch> = listOf(), // todo make List<T : ItemMatch>?
        val errors: List<String> = listOf(),
        val sorting: NodeSorting = NodeSorting.Date.Reversed,
        val generation: Int = 0,
    ) : SearchResult() {
        companion object {
            val Stub = Global(forText = false)
        }

        override fun getCounters(): IntArray = when {
            forText -> intArrayOf(count, matches.size, countTotal)
            else -> intArrayOf(matches.size)
        }

        fun toMarkdown(filter: ((ItemMatch) -> Boolean)? = null): String {
            val data = StringBuilder()
            for (item in matches) {
                if (filter != null && !filter(item)) {
                    continue
                }
                val path = item.ref.string.replace(" ", "\\ ")
                data.append("[${item.ref.name}]($path)\n")
            }
            return data.toString()
        }

        fun removeItems(removed: List<Node>): SearchResult {
            val nothing = matches.none { match ->
                removed.any { match.ref.isChildOf(it.ref) }
            }
            if (nothing) {
                return this
            }
            val left = matches.filter { match ->
                removed.none { match.ref.isChildOf(it.ref) }
            }
            val count = left.sumOf { it.count }
            return Global(forText, count = count, countTotal = left.size, left)
        }

        fun contains(match: ItemMatch) = matches.contains(match)

        override fun hashCode(): Int = hash(this::class, count, countTotal, errors.size, sorting)

        override fun equals(other: Any?): Boolean = when {
            other === this -> true
            other !is Global -> false
            other.count != count -> false
            other.countTotal != countTotal -> false
            other.generation != generation -> false
            other.sorting != sorting -> false
            other.errors.size != errors.size -> false
            else -> other::class == this::class
        }
    }
}