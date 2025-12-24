package app.atomofiron.searchboxapp.screens.explorer.fragment.list

import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinder
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ItemVisibilityDelegate

interface ExplorerItemActionListener :
    ExplorerItemBinder.ExplorerItemBinderActionListener,
    ItemVisibilityDelegate.ItemVisibilityListener<Node>