package app.atomofiron.searchboxapp.model.other

sealed interface UpdateType {
    sealed interface Variant : UpdateType

    data object Immediate : Variant
    data object Flexible : Variant
    data object All : UpdateType
}

sealed class AppUpdateState(val waiting: Boolean = false) {
    data object Unknown : AppUpdateState()
    data class Error(val message: String?) : AppUpdateState()
    data object UpToDate : AppUpdateState()
    data class Available(val type: UpdateType, val code: Int) : AppUpdateState(waiting = true)
    data class Downloading(val progress: Float?) : AppUpdateState()
    data object Completable : AppUpdateState(waiting = true)
    data object Installing : AppUpdateState()
}