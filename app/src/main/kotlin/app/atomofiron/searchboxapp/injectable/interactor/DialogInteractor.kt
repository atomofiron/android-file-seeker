package app.atomofiron.searchboxapp.injectable.interactor

import android.widget.Toast
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.injectable.service.UtilService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.utils.DialogBuilder

class DialogInteractor(
    private val appStore: AppStore,
    private val utils: UtilService,
) {
    private val resources by appStore.resourcesProperty

    fun builder() = DialogBuilder(appStore.activityProperty)

    fun showError(message: String) = DialogBuilder(appStore.activityProperty)
        .setTitle(R.string.error)
        .setMessage(message)
        .setPositiveButton(R.string.ok)
        .setNegativeButton(R.string.copy) {
            val toast = try {
                utils.copyToClipboard(resources.getString(R.string.error), message)
                resources.getString(R.string.copied)
            } catch (e: Exception) {
                e.toString()
            }
            Toast.makeText(appStore.context, toast, Toast.LENGTH_LONG).show()
        }.show()
}
