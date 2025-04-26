package app.atomofiron.searchboxapp.screens.finder.viewmodel

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KClass

interface FinderItemsState {
    /* 1 search/replace
     * characters
     * config (optional for finder screen)
     * test field
     * progressItems (only finder screen)
     * targetItems (only finder screen)
     */
    val uniqueItems: MutableList<FinderStateItem>
    val progressItems: MutableList<FinderStateItem.ProgressItem>
    val targets: MutableList<Node>
    val configItem: FinderStateItem.ConfigItem

    val searchItems: MutableStateFlow<List<FinderStateItem>>
    val isLocal: Boolean

    fun updateState()
    fun updateSearchQuery(value: String)
    fun updateConfig(item: FinderStateItem.ConfigItem)

    fun <I : FinderStateItem> getUniqueItem(kClass: KClass<I>): I
    fun <I : FinderStateItem> updateUniqueItem(item: I)
    fun <I : FinderStateItem> updateUniqueItem(kClass: KClass<I>, action: (I) -> I)
}