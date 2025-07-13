package app.atomofiron.searchboxapp.screens.finder.viewmodel

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.S
import app.atomofiron.common.util.flow.mapState
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.SearchTask
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.Buttons
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.Disclaimer
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.EditCharacters
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.MaxDepth
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.MaxSize
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.Options
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.Query
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.SpecialCharacters
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.Targets
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.TestField
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.Title
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class FinderItemsStateDelegate(
    isLocal: Boolean,
    preferences: PreferenceStore,
    tasks: Flow<List<SearchTask>>,
) : FinderItemsState {

    private val query = MutableStateFlow("")
    override val targets = MutableStateFlow<List<Node>>(mutableListOf())
    private val mutableToggles = MutableStateFlow(Options(isLocal = true))
    override val toggles = when {
        isLocal -> mutableToggles
        else -> preferences.searchOptions.mapState(::Options)
    }
    private val localOptions = toggles.map { listOf(it) }
    private val globalOptions = combine(
        toggles,
        preferences.specialCharacters,
        preferences.maxDepthForSearch,
        preferences.maxFileSizeForSearch,
        preferences.showSearchOptions,
    ) { config, chars, depth, size, show ->
        when {
            show -> listOf(
                config,
                MaxSize(size),
                MaxDepth(depth),
                EditCharacters(chars.toList()),
                Title(R.string.options_title),
            )
            else -> emptyList()
        }
    }
    override val items = combine(
        combine(
            query,
            preferences.testField,
            preferences.specialCharacters,
            if (isLocal) localOptions else globalOptions,
            toggles,
        ) { query, test, chars, options, config ->
            buildList {
                add(Query(query, useRegex = config.useRegex))
                add(SpecialCharacters(chars))
                if (!isLocal) add(Buttons)
                add(TestField(value = test, query = query, useRegex = config.useRegex, ignoreCase = config.ignoreCase))
                addAll(options)
            }
        },
        targets,
        tasks.map { it.map(FinderStateItem::Task).reversed() },
    ) { items, targets, tasks ->
        buildList {
            addAll(items)
            if (!isLocal && targets.isNotEmpty()) {
                val index = items.indexOfFirst { it is TestField }.inc()
                add(index, Title(R.string.search_here))
                add(index, Targets(targets.toList()))
            }
            if (SDK_INT >= S && !isLocal && tasks.any { it.task.withRetries }) {
                add(Disclaimer)
            }
            addAll(tasks)
        }
    }

    override fun updateSearchQuery(value: String) {
        query.value = value
    }

    override fun updateConfig(item: Options) {
        mutableToggles.value = item
    }

    override fun updateTargets(targets: List<Node>) {
        this.targets.value = targets
    }
}