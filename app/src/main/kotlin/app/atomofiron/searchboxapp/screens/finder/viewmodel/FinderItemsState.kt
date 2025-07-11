package app.atomofiron.searchboxapp.screens.finder.viewmodel

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface FinderItemsState {
    val targets: StateFlow<List<Node>>
    val toggles: StateFlow<FinderStateItem.Options>

    val items: Flow<List<FinderStateItem>>

    fun updateSearchQuery(value: String)
    fun updateConfig(item: FinderStateItem.Options)
    fun updateTargets(targets: List<Node>)
}