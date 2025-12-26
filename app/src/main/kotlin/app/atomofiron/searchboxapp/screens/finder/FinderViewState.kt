package app.atomofiron.searchboxapp.screens.finder

import android.view.Gravity
import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.di.dependencies.store.FinderStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.finder.di.history.HistoryDao
import app.atomofiron.searchboxapp.screens.finder.di.history.ItemHistory
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsState
import app.atomofiron.searchboxapp.screens.finder.viewmodel.FinderItemsStateDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@FinderScope
class FinderViewState @Inject constructor(
    private val scope: CoroutineScope,
    preferencesStore: PreferenceStore,
    val finderStore: FinderStore,
    history: HistoryDao,
) : FinderItemsState by FinderItemsStateDelegate(
    isLocal = false,
    preferencesStore,
    finderStore.tasksFlow,
) {

    val inactiveTargets = MutableStateFlow(emptyList<Int>())
    val historyDrawerGravity = MutableStateFlow(Gravity.START)
    val insertInQuery = ChannelFlow<String>()
    val replaceQuery = ChannelFlow<String>()
    val history: Flow<List<ItemHistory>> = history.flow
    val showHistory = EventFlow<Unit>()
    val permissionRequiredWarning = ChannelFlow<Unit>()

    fun showPermissionRequiredWarning() {
        permissionRequiredWarning(scope)
    }

    fun insertInQuery(value: String) {
        insertInQuery[scope] = value
    }

    fun replaceQuery(value: String) {
        replaceQuery[scope] = value
    }

    fun showHistory() = showHistory.invoke(scope)
}