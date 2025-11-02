package app.atomofiron.searchboxapp.model.finder

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeSorting
import app.atomofiron.searchboxapp.model.textviewer.MatchMap
import app.atomofiron.searchboxapp.utils.Const
import java.util.Objects
import kotlin.LazyThreadSafetyMode.NONE

sealed class SearchResult {

    abstract val count: Int
    abstract val countTotal: Int

    val isEmpty: Boolean get() = count == 0

    abstract fun getCounters(): IntArray

    data class Text(
        override val count: Int,
        val matches: MatchMap,
    ) : SearchResult() {

        val indexes: List<Int> by lazy(NONE) { matches.keys.sorted() }

        override val countTotal = 1

        constructor() : this(0, mapOf())

        override fun getCounters(): IntArray = intArrayOf(count)

        override fun hashCode(): Int = Objects.hash(this::class, count)

        override fun equals(other: Any?): Boolean = when {
            other === this -> true
            other !is Text -> false
            other.count != count -> false
            else -> other::class == this::class
        }
    }

    data class Files(
        private val forText: Boolean,
        override val count: Int = 0,
        override val countTotal: Int = 0,
        val matches: List<ItemMatch> = listOf(),
        val errors: List<Node> = listOf(),
        val sorting: NodeSorting = NodeSorting.Date.Reversed,
    ) : SearchResult() {
        companion object {
            val Stub = Files(forText = false)
        }

        override fun getCounters(): IntArray = when {
            forText -> intArrayOf(count, matches.size, countTotal)
            else -> intArrayOf(matches.size)
        }

        fun toMarkdown(checkedOnly: Boolean): String {
            val data = StringBuilder()
            for (item in matches) {
                if (checkedOnly && !item.isChecked) {
                    continue
                }
                val name = if (item.item.isDirectory) item.item.name + Const.SLASH else item.item.name
                val path = item.item.ref.string.replace(" ", "\\ ")
                data.append("[$name]($path)\n")
            }
            return data.toString()
        }

        fun removeItem(removed: Node): SearchResult {
            val nothing = !matches.any { it.item.ref.isChildOf(removed.ref) }
            if (nothing) return this
            val left = matches.filter { !it.item.ref.isChildOf(removed.ref) }
            val items = matches.toMutableList()
            val count = left.sumOf { it.count }
            return Files(forText, count = count, countTotal = countTotal.dec(), items)
        }

        fun contains(match: ItemMatch) = matches.contains(match)

        override fun hashCode(): Int = Objects.hash(this::class, count, countTotal, errors.size, sorting)

        override fun equals(other: Any?): Boolean = when {
            other === this -> true
            other !is Files -> false
            other.count != count -> false
            other.countTotal != countTotal -> false
            other.sorting != sorting -> false
            other.errors.size != errors.size -> false
            else -> other::class == this::class
        }
    }
}