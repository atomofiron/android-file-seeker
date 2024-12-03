package app.atomofiron.searchboxapp.model.other

sealed class AppUpdateState {
    object Unknown : AppUpdateState()
    object Error : AppUpdateState()
    object Available : AppUpdateState()
    object InProgress : AppUpdateState()
    object Downloaded : AppUpdateState()
    object UpToDate : AppUpdateState()
}