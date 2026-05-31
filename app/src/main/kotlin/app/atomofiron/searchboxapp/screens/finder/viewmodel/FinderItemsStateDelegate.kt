package app.atomofiron.searchboxapp.screens.finder.viewmodel

import app.atomofiron.common.util.flow.mapState
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.other.ByteSize
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.finder.SearchTask
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.Buttons
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.EditCharacters
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.EditOptions
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.MaxDepth
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.MaxSize
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.Query
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.SpecialCharacters
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.Targets
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.TestField
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.Title
import app.atomofiron.searchboxapp.utils.replaceOne
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FinderItemsStateDelegate<Result : SearchResult, Task : SearchTask<Result>>(
    override val isLocal: Boolean,
    preferences: PreferenceStore,
    tasks: Flow<List<Task>>,
) : FinderItemsState {

    private val query = MutableStateFlow("")
    override val targets = MutableStateFlow<List<Node>>(mutableListOf())
    override val toggles = (if (isLocal) preferences.localSearchOptions else preferences.searchOptions).mapState(::EditOptions)
    private val localOptions = toggles.map { listOf(it) }
    private val globalOptions = combine(
        toggles,
        preferences.specialCharacters,
        preferences.maxDepthForSearch,
        preferences.maxFileSizeForSearch,
        preferences.showSearchOptions,
        ::composeOptions,
    )
    private val uniqueItems = combine(
        query,
        preferences.testField,
        preferences.specialCharacters,
        if (isLocal) localOptions else globalOptions,
        toggles,
        ::composeUniqueItems,
    )
    override val items = combine(uniqueItems, targets, tasks, ::composeAllItems)

    private fun composeOptions(config: EditOptions, chars: Array<String>, depth: Int, size: ByteSize, show: Boolean) = when {
        show -> listOf(
            config,
            MaxSize(size, enabled = config.toggles.contentSearch),
            MaxDepth(depth),
            EditCharacters(chars.toList()),
            Title(R.string.options_title),
        )
        else -> listOf(FinderStateItem.Options(config.toggles))
    }

    private fun composeUniqueItems(query: String, test: String?, chars: Array<String>, options: List<FinderStateItem>, config: EditOptions): List<FinderStateItem> {
        return buildList {
            add(Query(query, regex = config.regex))
            if (chars.isNotEmpty()) add(SpecialCharacters(chars))
            if (!isLocal) add(Buttons)
            add(TestField(value = test, query = query, regex = config.regex, ignoreCase = config.ignoreCase))
            addAll(options)
        }
    }

    private fun composeAllItems(items: List<FinderStateItem>, targets: List<Node>, tasks: List<Task>): List<FinderStateItem> {
        return buildList {
            addAll(items)
            replaceOne<Query, _> { copy(enabled = query.isNotEmpty() && (isLocal || targets.any { it.isChecked })) }
            if (!isLocal && targets.isNotEmpty()) {
                val index = items.indexOfFirst { it is TestField }.inc()
                add(index, Title(R.string.search_here))
                add(index, Targets(targets.toList()))
            }
            addAll(tasks.reversed().map { FinderStateItem.Task(it, clickableIfEmpty = !isLocal) })
        }
    }

    override fun updateSearchQuery(value: String) {
        query.value = value
    }

    override fun updateTargets(items: List<Node>) {
        targets.value = items
    }
}