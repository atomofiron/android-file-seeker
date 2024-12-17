package app.atomofiron.searchboxapp.model.other

sealed interface UpdateNotification {
    data object Available : UpdateNotification
    data object Install : UpdateNotification
}
