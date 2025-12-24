package app.atomofiron.searchboxapp.screens.explorer.fragment

import app.atomofiron.common.util.Alert
import app.atomofiron.searchboxapp.model.explorer.Node

sealed class ExplorerAlert(
    error: Boolean = false,
    important: Boolean = false,
) : Alert.Other(error, important) {
    data class Deleted(val items: List<Node>) : ExplorerAlert()
}