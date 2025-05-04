package app.atomofiron.searchboxapp.injectable.interactor

import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.injectable.service.UtilService
import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.utils.DialogBuilder

class DialogInteractor(
    appStore: AppStore,
    private val utils: UtilService,
) : AppStore by appStore {

    fun builder() = DialogBuilder(activityProperty)

    fun showError(message: String) = DialogBuilder(activityProperty)
        .setTitle(R.string.error)
        .setMessage(message)
        .setPositiveButton(R.string.ok)
        .setNegativeButton(R.string.copy) {
            utils.copyToClipboard(resources.getString(R.string.error), message)
        }.show()
}
