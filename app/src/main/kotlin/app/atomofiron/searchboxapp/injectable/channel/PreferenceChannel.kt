package app.atomofiron.searchboxapp.injectable.channel

import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.injectable.AppScope
import app.atomofiron.searchboxapp.model.other.AppUpdateState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class PreferenceChannel(
    private val scope: AppScope,
    updates: StateFlow<AppUpdateState>,
) {
    val onHistoryImported = EventFlow<Unit>()
    val appUpdateStatus = EventFlow<String>()
    val notification: Flow<Boolean> = updates.map { it.waiting }

    fun notifyHistoryImported() = onHistoryImported.invoke(scope)
    fun notifyUpdateStatus(message: String) = appUpdateStatus.set(scope, message)
}