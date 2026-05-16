package app.atomofiron.searchboxapp.model.other

import app.atomofiron.common.util.TaskId

sealed interface AppScreen {
    data object Unknown : AppScreen
    data object Explorer : AppScreen
    data object Finder : AppScreen
    data object Settings : AppScreen
    data class Results(val taskId: TaskId) : AppScreen
    data object TextViewer : AppScreen
}