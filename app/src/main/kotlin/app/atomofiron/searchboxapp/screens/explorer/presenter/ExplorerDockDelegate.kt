package app.atomofiron.searchboxapp.screens.explorer.presenter

import android.Manifest.permission.POST_NOTIFICATIONS
import androidx.work.WorkManager
import app.atomofiron.common.util.Unreachable
import app.atomofiron.common.util.extension.launchOnMain
import app.atomofiron.searchboxapp.custom.view.dock.item.DockItem
import app.atomofiron.searchboxapp.di.dependencies.channel.PreferenceChannel
import app.atomofiron.searchboxapp.di.dependencies.router.FilePickingDelegate
import app.atomofiron.searchboxapp.di.dependencies.router.startReceiveInto
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.screens.explorer.ExplorerRouter
import app.atomofiron.searchboxapp.screens.explorer.ExplorerScope
import app.atomofiron.searchboxapp.screens.explorer.fragment.ExplorerDockListener
import app.atomofiron.searchboxapp.screens.explorer.state.ExplorerDockState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@ExplorerScope
class ExplorerDockDelegate @Inject constructor(
    private val mode: ActivityMode,
    private val router: ExplorerRouter,
    private val sharing: FilePickingDelegate,
    private val store: ExplorerStore,
    preferenceChannel: PreferenceChannel,
    private val workManager: WorkManager,
    private val scope: CoroutineScope,
) : ExplorerDockListener {

    val dock: Flow<List<DockItem>> = combine(store.currentDeepest, preferenceChannel.notification, store.checked, transform = ::dockItems)

    override fun onSearchClick() = router.showFinder()

    override fun onSettingsClick() = router.showSettings()

    override fun onConfirmClick() {
        when (mode) {
            is ActivityMode.Default -> Unreachable
            is ActivityMode.Receive -> receive(mode)
            is ActivityMode.Share -> share(mode.multiple)
        }
    }

    private fun dockItems(currentDir: Node?, notice: Boolean, checked: List<Node>): List<DockItem> {
        val checked = checked.filter { it.isFile }
        return ExplorerDockState {
            add(it.search)
            when (mode) {
                is ActivityMode.Default -> add(it.settings.copy(notice = notice))
                is ActivityMode.Receive -> add(it.confirm.copy(enabled = currentDir?.isDirectory == true))
                is ActivityMode.Share -> add(it.confirm.copy(enabled = currentDir?.isFile == true || checked.isNotEmpty() && (mode.multiple || checked.size == 1)))
            }
        }
    }

    private fun receive(mode: ActivityMode.Receive) {
        val destination = store.currentDeepest.value?.ref
        destination ?: return
        router.permissions
            .request(POST_NOTIFICATIONS)
            .any {
                scope.launchOnMain {
                    workManager.startReceiveInto(destination, mode)
                    router.finish()
                }
            }
    }

    private fun share(multiple: Boolean) {
        val items = checkedFiles() ?: return
        when {
            multiple -> sharing.shareMultiplePicked(items)
            else -> sharing.shareSinglePicked(items.first())
        }
    }

    private fun checkedFiles() = store.checked.value
        .filter { it.isFile }
        .ifEmpty { null }
}