package app.atomofiron.searchboxapp.screens.finder.viewmodel

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.S
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KClass

class FinderItemsStateDelegate(override val isLocal: Boolean) : FinderItemsState {
    override val uniqueItems = mutableListOf<FinderStateItem>()
    override val progressItems = mutableListOf<FinderStateItem.ProgressItem>()
    override val targets = mutableListOf<Node>()
    override val searchItems = MutableStateFlow<List<FinderStateItem>>(listOf())
    override var configItem = FinderStateItem.ConfigItem()

    // todo add state machine
    override fun updateState() {
        val items = mutableListOf<FinderStateItem>()
        items.addAll(uniqueItems)
        when {
            isLocal -> Unit
            targets.isEmpty() -> items.add(FinderStateItem.TipItem(R.string.tip))
            else -> {
                items.add(FinderStateItem.TargetsItem(targets.toList()))
                items.add(FinderStateItem.TipItem(R.string.search_here))
            }
        }
        if (SDK_INT >= S && !isLocal && progressItems.any { it.task.withRetries }) {
            items.add(FinderStateItem.DisclaimerItem)
        }
        items.addAll(progressItems)
        searchItems.value = items
    }

    override fun updateSearchQuery(value: String) {
        updateUniqueItem(FinderStateItem.TestItem::class) {
            it.copy(searchQuery = value)
        }
        val item = getUniqueItem(FinderStateItem.SearchAndReplaceItem::class)
        item.query = value
        // do not notify
    }

    override fun updateConfig(item: FinderStateItem.ConfigItem) {
        val prevConfigItem = configItem
        configItem = item
        updateUniqueItem(item)

        val ignoreCaseChanged = prevConfigItem.ignoreCase xor item.ignoreCase
        val replaceEnabledChanged = prevConfigItem.replaceEnabled xor item.replaceEnabled
        val useRegexpChanged = prevConfigItem.useRegex xor item.useRegex
        val multilineSearchChanged = prevConfigItem.excludeDirs xor item.excludeDirs

        if (replaceEnabledChanged || useRegexpChanged) {
            updateUniqueItem(FinderStateItem.SearchAndReplaceItem::class) {
                it.copy(replaceEnabled = item.replaceEnabled, useRegex = item.useRegex)
            }
        }
        if (ignoreCaseChanged || useRegexpChanged || multilineSearchChanged) {
            updateUniqueItem(FinderStateItem.TestItem::class) {
                it.copy(useRegex = item.useRegex, ignoreCase = item.ignoreCase)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <I : FinderStateItem> getUniqueItem(kClass: KClass<I>): I {
        return uniqueItems.find { it::class == kClass } as I
    }

    override fun <I : FinderStateItem> updateUniqueItem(item: I) {
        val index = uniqueItems.indexOfFirst { it::class == item::class }
        if (index < 0) return
        uniqueItems.removeAt(index)
        uniqueItems.add(index, item)
        updateState()
    }

    override fun <I : FinderStateItem> updateUniqueItem(kClass: KClass<I>, action: (I) -> I) {
        val index = uniqueItems.indexOfFirst { it::class == kClass }
        var item = uniqueItems[index]
        @Suppress("UNCHECKED_CAST")
        item = action(item as I)
        uniqueItems[index] = item
        updateState()
    }
}