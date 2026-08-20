package app.atomofiron.searchboxapp.android

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.atomofiron.common.util.extension.get
import app.atomofiron.common.util.extension.logD
import app.atomofiron.common.util.extension.logE
import app.atomofiron.common.util.extension.nil
import app.atomofiron.common.util.extension.put
import app.atomofiron.common.util.flow.collect
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.channel.CommonChannel
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.other.AppState
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.canManageFiles
import app.atomofiron.searchboxapp.utils.canPostNotifications
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import uniffi.native_lib.FileEvent
import uniffi.native_lib.WatchHandle
import javax.inject.Inject

class ScreenshotServiceDependencies @Inject constructor(
    val scope: AppScope,
    val preferences: PreferenceStore,
    val explorerStore: ExplorerStore,
    val commonChannel: CommonChannel,
)

class ScreenshotService : Service() {
    companion object {
        val error: StateFlow<String?>
            field = MutableStateFlow<String?>(null)

        fun Context.initScreenshotService(deps: ScreenshotServiceDependencies) {
            combine(
                deps.preferences.screenshotOperations,
                deps.explorerStore.screenshots,
                deps.commonChannel.appState,
            ) { enable, target, state ->
                val intent = Intent(this, ScreenshotService::class.java)
                target?.let { intent.put(it) }
                error.value = when {
                    target == null || !enable -> stopService(intent)
                        .nil()
                    state != AppState.Foreground -> return@combine
                    !canManageFiles() -> getString(R.string.no_storage_permission)
                    !canPostNotifications() -> getString(R.string.no_notification_permission)
                    else -> ContextCompat.startForegroundService(this, intent)
                        .nil()
                }
            }.collect(deps.scope)
        }
    }

    private val notifications by lazy { NotificationManagerCompat.from(this) }
    private var handle: WatchHandle? = null
    private var target: NodeRef? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val target = intent?.get<NodeRef>()
        when {
            target == null -> stopSelf()
            target != this.target -> tryStart(target)
            else -> logD("screenshot service is already running")
        }
        return START_REDELIVER_INTENT
    }

    private fun tryStart(target: NodeRef) {
        clear()
        startScreenshotsForeground()
        this.target = target
        val result = NativeBridge.observeDir(target) {
            onEventResult(it, target)
        }
        handle = result.ok()?.value
        error.value = when (result) {
            is Rslt.Ok -> null
            is Rslt.Err -> {
                stopSelf()
                result.message
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clear()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun clear() {
        handle?.stop()
        handle = null
    }

    private fun onEventResult(result: Rslt<FileEvent>, dir: NodeRef) = when (result) {
        is Rslt.Ok -> onEvent(result.value, dir)
        is Rslt.Err -> logE("Dir observing error: ${result.message}")
    }

    private fun onEvent(event: FileEvent, dir: NodeRef) {
        when (event) {
            is FileEvent.Create -> showScreenshotOperations(dir + event.v1)
            is FileEvent.Delete -> notifications.cancel((dir + event.v1).uniqueId)
            is FileEvent.Move -> {
                val oldId = (dir + event.from).uniqueId
                val newRef = dir + event.to
                val old = notifications.activeNotifications.find { it.id == oldId }
                old ?: return
                notifications.cancel(oldId)
                showScreenshotOperations(newRef)
            }
        }
    }
}
