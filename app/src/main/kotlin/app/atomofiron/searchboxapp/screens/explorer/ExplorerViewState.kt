package app.atomofiron.searchboxapp.screens.explorer

import app.atomofiron.common.util.flow.*
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.interactor.ExplorerInteractor
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.*
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
    val rootOptions = listOf(R.id.menu_create)
    val directoryOptions = listOf(R.id.menu_delete, R.id.menu_rename, R.id.menu_create)
    val oneFileOptions = listOf(R.id.menu_delete, R.id.menu_rename, R.id.menu_share, R.id.menu_open_with)
    val manyFilesOptions = listOf(R.id.menu_delete)

    val scrollTo = ChannelFlow<Node>()
    val settingsNotification = preferenceChannel.notification
    val itemComposition = DeferredStateFlow<ExplorerItemComposition>()
    val current: StateFlow<Node?> = explorerStore.current
    val alerts: Flow<NodeError> = explorerStore.alerts

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
}