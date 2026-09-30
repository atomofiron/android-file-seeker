package app.atomofiron.searchboxapp.screens.finder.viewmodel

import app.atomofiron.common.util.flow.mapState
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.di.dependencies.store.SupportedCharsets
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.finder.SearchTask
import app.atomofiron.searchboxapp.model.finder.SelectableCharset
import app.atomofiron.searchboxapp.model.other.ByteSize
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.Buttons
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.Charsets
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.EditCharacters
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.EditOptions
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import uniffi.native_lib.SupportedCharset

class FinderItemsStateDelegate<Result : SearchResult, Task : SearchTask<Result>>(
    override val isLocal: Boolean,
    override val charset: StateFlow<String>,
    preferences: PreferenceStore,
    charsets: SupportedCharsets,
    tasks: Flow<List<Task>?>,
) : FinderItemsState {

    private val query = MutableStateFlow("")
    override val targets = MutableStateFlow<List<Node>>(mutableListOf())
    override val toggles = (if (isLocal) preferences.localSearchOptions else preferences.searchOptions).mapState(::EditOptions)
    private val localOptions: Flow<List<FinderStateItem>> = toggles.map { listOf<FinderStateItem>(it) }

    private val firstItems = combine(
        query,
        preferences.specialCharacters,
        preferences.testField,
        targets,
        toggles,
    ) { query, chars, test, targets, config ->
        buildList {
            add(Query(query, regex = config.regex, enabled = query.isNotEmpty() && (isLocal || targets.any { it.isChecked })))
            if (chars.isNotEmpty()) add(SpecialCharacters(chars))
            if (!isLocal) add(Buttons(withTest = test == ""))
            if (test != "") add(TestField(value = test, query = query, regex = config.regex, ignoreCase = config.ignoreCase))
            if (!isLocal && targets.isNotEmpty()) {
                add(Targets(targets.toList()))
                add(Title(R.string.search_here))
            }
        }
    }
    private val globalOptions = combine(
        toggles,
        combine(charsets.list, charset, toggles, ::selectableCharsets),
        combine(
            preferences.specialCharacters,
            preferences.maxDepthForSearch,
            preferences.maxFileSizeForSearch
        ) { a, b, c -> Triple(a, b, c) },
        charset,
        preferences.showSearchOptions,
        ::composeOptions,
    )
    override val items = combine(
        firstItems,
        if (isLocal) localOptions else globalOptions,
        tasks.map { task -> task?.reversed()?.map { FinderStateItem.Task(it, clickableIfEmpty = !isLocal) } },
    ) { first, options, tasks ->
        buildList {
            addAll(first)
            addAll(options)
            tasks?.let { addAll(tasks) }
                ?: add(FinderStateItem.Loading)
        }
    }


    private fun selectableCharsets(charsets: List<SupportedCharset>, selected: String?, options: EditOptions): List<SelectableCharset> {
        return charsets.map { SelectableCharset(it.name, selected = it.name == selected, enabled = options.contentSearch) }
    }

    private fun composeOptions(
        options: EditOptions,
        charsets: List<SelectableCharset>,
        settings: Triple<Array<String>, Int, ByteSize>,
        charset: String,
        show: Boolean,
    ) = when {
        show -> listOf(
            Charsets(charsets),
            options,
            MaxSize(settings.third, enabled = options.toggles.contentSearch),
            MaxDepth(settings.second),
            EditCharacters(settings.first.toList()),
            Title(R.string.options_title),
        )
        else -> listOf(Options(options.toggles, charset))
    }

    override fun updateSearchQuery(value: String) {
        query.value = value
    }

    override fun updateTargets(items: List<Node>) {
        targets.value = items
    }
}