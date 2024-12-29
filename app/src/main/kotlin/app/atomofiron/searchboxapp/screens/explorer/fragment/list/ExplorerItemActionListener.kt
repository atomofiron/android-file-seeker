package app.atomofiron.searchboxapp.screens.explorer.fragment.list

import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ItemVisibilityDelegate

interface ExplorerItemActionListener :
    ExplorerItemBinderImpl.ExplorerItemBinderActionListener,
    ItemVisibilityDelegate.ExplorerItemVisibilityListener