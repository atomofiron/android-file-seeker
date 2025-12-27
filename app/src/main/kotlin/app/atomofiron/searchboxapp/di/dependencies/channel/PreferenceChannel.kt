package app.atomofiron.searchboxapp.di.dependencies.channel

import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.set
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.store.AppUpdateStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceChannel @Inject constructor(
    private val scope: AppScope,
    updates: AppUpdateStore,
) {
    val onHistoryImported = EventFlow<Unit>()
    val appUpdateStatus = EventFlow<String>()
    val notification: Flow<Boolean> = updates.state.map { it.waiting }

    fun notifyHistoryImported() = onHistoryImported.invoke(scope)
    fun notifyUpdateStatus(message: String) = appUpdateStatus.set(scope, message)
}