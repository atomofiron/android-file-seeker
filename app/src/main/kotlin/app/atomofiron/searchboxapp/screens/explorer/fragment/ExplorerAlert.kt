package app.atomofiron.searchboxapp.screens.explorer.fragment

import app.atomofiron.searchboxapp.model.explorer.Node

sealed interface ExplorerAlert {
    data class Deleted(val items: List<Node>)
}