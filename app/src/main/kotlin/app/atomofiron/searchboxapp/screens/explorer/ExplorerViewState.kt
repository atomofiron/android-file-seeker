package app.atomofiron.searchboxapp.screens.explorer

import app.atomofiron.common.util.Alert
import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.screens.explorer.di.ExplorerInteractor
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerAlert
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerDockDelegate
import app.atomofiron.searchboxapp.utils.toUni
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

@ExplorerScope
class ExplorerViewState @Inject constructor(
    private val scope: CoroutineScope,
    val mode: ActivityMode,
    dockDelegate: ExplorerDockDelegate,
    private val store: ExplorerStore,
    interactor: ExplorerInteractor,
    preferences: PreferenceStore,
) {
    private val mimeTypes = mode.mimeFilters()
    val tabs = store.mainTabs.map { it.copy(pickerTypes = mimeTypes) }

    val scrollTo = ChannelFlow<Node>()
    val itemComposition = preferences.explorerItemComposition
    private val otherAlerts = ChannelFlow<Alert>()
    val alerts: Flow<Alert> = merge(
        store.alerts.map { Alert(it.toUni()) },
        store.deleted.mapNotNull { deleted ->
            deleted.takeIf { it.isNotEmpty() }
                ?.let { ExplorerAlert.Deleted(it) }
        },
        otherAlerts,
    )
    val currentTab = MutableStateFlow(tabs[store.currentTabKey.value.index])
    val currentNode get() = store.currentDeepest.value

    val currentTabFlow = interactor.getFlow(currentTab.value)
    val updates: Flow<Node> = store.updated
    val permissionRequiredWarning = ChannelFlow<Unit>()
    val dock: Flow<List<DockItem>> = dockDelegate.dock

    fun showPermissionRequiredWarning() = permissionRequiredWarning(scope)

    fun scrollTo(item: Node) {
        scrollTo[scope] = item
    }

    fun showAlert(message: Alert) {
        otherAlerts[scope] = message
    }
}