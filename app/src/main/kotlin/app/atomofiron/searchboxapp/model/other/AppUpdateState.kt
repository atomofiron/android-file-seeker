package app.atomofiron.searchboxapp.model.other

enum class UpdateType {
    Immediate, Flexible, All
}

sealed interface AppUpdateState {
    data object Unknown : AppUpdateState
    data object UpToDate : AppUpdateState
    data class Available(val type: UpdateType) : AppUpdateState
    data class Downloading(val progress: Float) : AppUpdateState
    data object Installing : AppUpdateState // Flexible
    data object Completable : AppUpdateState
}