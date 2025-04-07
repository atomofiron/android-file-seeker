package app.atomofiron.searchboxapp.screens.explorer

import app.atomofiron.common.util.AlertMessage
import app.atomofiron.common.util.flow.*
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.*
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

class ExplorerViewState(
    private val scope: CoroutineScope,
    explorerStore: ExplorerStore,
    explorerInteractor: ExplorerInteractor,
    preferenceChannel: PreferenceChannel,
) {
    companion object{
        private const val FIRST_TAB = "FIRST_TAB"
        private const val SECOND_TAB = "SECOND_TAB"
    }

    val scrollTo = ChannelFlow<Node>()
    val settingsNotification = preferenceChannel.notification
    val itemComposition = DeferredStateFlow<ExplorerItemComposition>()
    private val otherAlerts = ChannelFlow<AlertMessage>()
    val alerts: Flow<AlertMessage> = merge(explorerStore.alerts.map { AlertMessage(it) }, otherAlerts)

    val firstTab = NodeTabKey(FIRST_TAB, index = 0)
    val secondTab = NodeTabKey(SECOND_TAB, index = 1)
    val currentTab = MutableStateFlow(firstTab)

    val firstTabItems = explorerInteractor.getFlow(firstTab)
    //val secondTabItems = explorerInteractor.getFlow(secondTab)

    fun scrollTo(item: Node) {
        scrollTo[scope] = item
    }

    fun getCurrentDir(): Node? {
        return when (currentTab.value) {
            firstTab -> firstTabItems.valueOrNull?.current
            else -> null//secondTabItems.valueOrNull?.current
        }
    }

    fun showAlert(message: AlertMessage) {
        otherAlerts[scope] = message
    }
}