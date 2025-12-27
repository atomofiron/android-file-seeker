package app.atomofiron.searchboxapp.screens.preferences.presenter

import app.atomofiron.searchboxapp.custom.preference.UpdateActionListener
import app.atomofiron.searchboxapp.di.dependencies.service.AppUpdateService
import app.atomofiron.searchboxapp.model.other.AppUpdateAction
import app.atomofiron.searchboxapp.screens.preferences.PreferenceScope
import javax.inject.Inject

@PreferenceScope
class UpdatePresenterDelegate @Inject constructor(
    private val service: AppUpdateService,
) : UpdateActionListener {
    override fun invoke(action: AppUpdateAction) {
        when (action) {
            is AppUpdateAction.Check -> service.check(userAction = true)
            is AppUpdateAction.Retry -> service.retry()
            is AppUpdateAction.Download -> service.startUpdate(action.choice)
            is AppUpdateAction.Install -> service.completeUpdate()
        }
    }
}