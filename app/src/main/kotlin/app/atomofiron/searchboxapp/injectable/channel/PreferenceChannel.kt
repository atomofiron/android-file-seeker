package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.set
import kotlinx.coroutines.CoroutineScope

class PreferenceChannel(
    private val scope: CoroutineScope,
) {
    val onHistoryImported = EventFlow<Unit>()
    val appUpdateStatus = EventFlow<String>()

    fun notifyHistoryImported() = onHistoryImported.invoke(scope)
    fun notifyUpdateStatus(message: String) = appUpdateStatus.set(scope, message)
}