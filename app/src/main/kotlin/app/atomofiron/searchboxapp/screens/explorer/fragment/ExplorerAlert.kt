package app.atomofiron.searchboxapp.screens.explorer.fragment

import app.atomofiron.common.util.Alert
import app.atomofiron.searchboxapp.model.explorer.Node

sealed class ExplorerAlert(mod: Alert.Mod) : Alert.Other(mod) {
    data class Deleted(val items: List<Node>) : ExplorerAlert(Alert.Mod.Important)
}