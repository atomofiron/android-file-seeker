package app.atomofiron.searchboxapp.model.other

sealed interface UpdateType {
    sealed interface Variant : UpdateType

    data object Immediate : Variant
    data object Flexible : Variant
    data object All : UpdateType
}

sealed interface AppUpdateState {
    data object Unknown : AppUpdateState
    data object UpToDate : AppUpdateState
    data class Available(val type: UpdateType) : AppUpdateState
    data class Downloading(val progress: Float) : AppUpdateState
    data object Installing : AppUpdateState // Flexible
    data object Completable : AppUpdateState
}