package app.atomofiron.searchboxapp.model.other

sealed interface AppUpdateAction {
    data object Check : AppUpdateAction
    data object Retry : AppUpdateAction
    data class Download(val choice: UpdateType.Variant) : AppUpdateAction
    data object Install : AppUpdateAction
}