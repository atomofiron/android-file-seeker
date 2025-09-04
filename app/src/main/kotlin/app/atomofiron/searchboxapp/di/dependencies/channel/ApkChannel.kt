package app.atomofiron.searchboxapp.di.dependencies.channel

import app.atomofiron.common.util.flow.EventFlow
import app.atomofiron.common.util.flow.set
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.dependencies.AppScope
import app.atomofiron.searchboxapp.di.dependencies.store.AppResources

class ApkChannel(
    private val appScope: AppScope,
    private val resources: AppResources,
) {
    val errorMessage = EventFlow<String>()
    val offerPackageName = EventFlow<String>()

    fun errorMessage(text: String?) {
        errorMessage[appScope] = text ?: resources().getString(R.string.unknown_error)
    }

    fun offerPackageName(packageName: String?) {
        offerPackageName[appScope] = packageName ?: return
    }
}
