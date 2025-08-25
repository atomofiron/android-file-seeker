package app.atomofiron.searchboxapp.screens.finder.state

import androidx.annotation.StringRes
import app.atomofiron.common.recycler.GeneralItem
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.ISearchConfig
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.model.finder.SearchTask
import java.util.Objects

sealed class FinderStateItem(
    val type: FinderItemType,
    override val stableId: Long = type.id.toLong(),
) : GeneralItem {
    val viewType = type.id

    constructor(type: FinderItemType, stableId: Int) : this(type, stableId.toLong())

    data class Query(
        val query: String = "",
        val useRegex: Boolean = false,
        val withReplace: Boolean = false,
        val enabled: Boolean = false,
    ) : FinderStateItem(FinderItemType.FIND)

    data class SpecialCharacters(
        val characters: Array<String>,
    ) : FinderStateItem(FinderItemType.CHARACTERS) {
        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is SpecialCharacters -> false
            else -> characters.contentEquals(other.characters)
        }
        override fun hashCode(): Int = Objects.hash(this::class, characters)
    }

    data class Title(@StringRes val stringId: Int) : FinderStateItem(FinderItemType.TITLE, stringId)

    data class Options(
        val toggles: SearchOptions,
        val isLocal: Boolean,
    ) : FinderStateItem(FinderItemType.EDIT_OPTIONS_MINI), ISearchConfig by toggles

    data class EditOptions(
        val toggles: SearchOptions,
        val isLocal: Boolean = false,
    ) : FinderStateItem(FinderItemType.EDIT_OPTIONS), ISearchConfig by toggles

    data class MaxDepth(val value: Int) : FinderStateItem(FinderItemType.MAX_DEPTH)

    data class MaxSize(val value: Int) : FinderStateItem(FinderItemType.MAX_SIZE)

    data class EditCharacters(val value: List<String>) : FinderStateItem(FinderItemType.EDIT_CHARS)

    data object Buttons : FinderStateItem(FinderItemType.BUTTONS)

    data class TestField(
        val value: String? = null,
        val query: String = "",
        val useRegex: Boolean = false,
        val ignoreCase: Boolean = true,
    ) : FinderStateItem(FinderItemType.TEST)

    data class Task(
        val task: SearchTask,
        val clickableIfEmpty: Boolean,
    ) : FinderStateItem(FinderItemType.PROGRESS, task.uniqueId)

    data class Targets(val targets: List<Node>) : FinderStateItem(FinderItemType.TARGETS)

    data object Disclaimer : FinderStateItem(FinderItemType.DISCLAIMER)

    companion object {
        val groups = listOf(
            listOf(Query::class, SpecialCharacters::class),
            listOf(TestField::class, Buttons::class),
            listOf(EditOptions::class),
            listOf(MaxDepth::class, MaxSize::class, EditCharacters::class),
        )
    }
}