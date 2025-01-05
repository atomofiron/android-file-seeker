package app.atomofiron.searchboxapp.injectable.interactor

import app.atomofiron.searchboxapp.injectable.store.AppStore
import app.atomofiron.searchboxapp.utils.DialogBuilder

class DialogInteractor(private val appStore: AppStore) {
    fun builder() = DialogBuilder(appStore.activityProperty)
}
