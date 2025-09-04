package app.atomofiron.searchboxapp.screens.finder.viewmodel

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.S
import app.atomofiron.common.util.flow.mapState
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.model.finder.SearchTask
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.Buttons
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.Disclaimer
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.EditCharacters
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.MaxDepth
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.MaxSize
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.EditOptions
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

class FinderItemsStateDelegate(
    override val isLocal: Boolean,
    preferences: PreferenceStore,
    tasks: Flow<List<SearchTask>>,
) : FinderItemsState {

    private val query = MutableStateFlow("")
    override val targets = MutableStateFlow<List<Node>>(mutableListOf())
    private val localToggles = MutableStateFlow(EditOptions(SearchOptions()))
    override val toggles = if (isLocal) localToggles else preferences.searchOptions.mapState(::EditOptions)
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

    private fun composeOptions(config: EditOptions, chars: Array<String>, depth: Int, size: Int, show: Boolean) = when {
        show -> listOf(
            config,
            MaxSize(size),
            MaxDepth(depth),
            EditCharacters(chars.toList()),
            Title(R.string.options_title),
        )
        else -> listOf(FinderStateItem.Options(config.toggles))
    }

    private fun composeUniqueItems(query: String, test: String?, chars: Array<String>, options: List<FinderStateItem>, config: EditOptions): List<FinderStateItem> {
        return buildList {
            add(Query(query, useRegex = config.useRegex))
            add(SpecialCharacters(chars))
            if (!isLocal) add(Buttons)
            add(TestField(value = test, query = query, useRegex = config.useRegex, ignoreCase = config.ignoreCase))
            addAll(options)
        }
    }

    private fun composeAllItems(items: List<FinderStateItem>, targets: List<Node>, tasks: List<SearchTask>): List<FinderStateItem> {
        return buildList {
            addAll(items)
            replaceOne<Query, _> { copy(enabled = query.isNotEmpty() && (isLocal || targets.any { it.isChecked })) }
            if (!isLocal && targets.isNotEmpty()) {
                val index = items.indexOfFirst { it is TestField }.inc()
                add(index, Title(R.string.search_here))
                add(index, Targets(targets.toList()))
            }
            val index = tasks.indexOfLast { it.withRetries }
            addAll(tasks.reversed().map { FinderStateItem.Task(it, clickableIfEmpty = !isLocal) })
            if (SDK_INT >= S && !isLocal && index >= 0) {
                add(tasks.lastIndex - index, Disclaimer)
            }
        }
    }

    override fun updateSearchQuery(value: String) {
        query.value = value
    }

    override fun updateConfig(options: SearchOptions) {
        localToggles.run {
            value = value.copy(toggles = options)
        }
    }

    override fun updateTargets(items: List<Node>) {
        targets.value = items
    }
}