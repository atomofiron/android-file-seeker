package app.atomofiron.searchboxapp.screens.explorer

import app.atomofiron.common.util.AlertMessage
import app.atomofiron.common.util.flow.ChannelFlow
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.di.dependencies.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeTabKey
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerAlert
import app.atomofiron.searchboxapp.screens.explorer.presenter.ExplorerDockDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class ExplorerViewState(
    private val scope: CoroutineScope,
    val mode: ActivityMode,
    explorerDockDelegate: ExplorerDockDelegate,
    private val store: ExplorerStore,
    explorerInteractor: ExplorerInteractor,
    preferenceStore: PreferenceStore,
) {
    private val mimeTypes = when (mode) {
        is ActivityMode.Default -> null
        is ActivityMode.Share -> mode.mimes
        is ActivityMode.Receive -> listOf(NodeContent.Directory.MIME_TYPE)
    }
    val firstTab = NodeTabKey.Explorer(0, mimeTypes)
    val middleTab = NodeTabKey.Explorer(1, mimeTypes)
    val lastTab = NodeTabKey.Explorer(2, mimeTypes)

    val scrollTo = ChannelFlow<Node>()
    val itemComposition = preferenceStore.explorerItemComposition
    private val otherAlerts = ChannelFlow<AlertMessage>()
    val alerts: Flow<AlertMessage> = merge(
        store.alerts.map { AlertMessage(it) },
        store.deleted.map { AlertMessage(ExplorerAlert.Deleted(it)) },
        otherAlerts,
    )
    val tabs = listOf(firstTab, middleTab, lastTab)
    val currentTab = MutableStateFlow(firstTab)
    val currentNode get() = store.currentNode.value

    val currentTabFlow = explorerInteractor.getFlow(firstTab)
    val updates: Flow<Node> = store.updated
    val permissionRequiredWarning = ChannelFlow<Unit>()
    val dock: Flow<List<DockItem>> = explorerDockDelegate.dock

    fun showPermissionRequiredWarning() = permissionRequiredWarning(scope)

    fun scrollTo(item: Node) {
        scrollTo[scope] = item
    }

    fun showAlert(message: AlertMessage) {
        otherAlerts[scope] = message
    }
}