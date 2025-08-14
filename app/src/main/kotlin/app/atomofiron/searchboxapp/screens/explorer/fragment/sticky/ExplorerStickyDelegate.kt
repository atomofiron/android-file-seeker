package app.atomofiron.searchboxapp.screens.explorer.fragment.sticky

import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerAdapter
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl.ExplorerItemBinderActionListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootAdapter

class ExplorerStickyDelegate(
    recyclerView: RecyclerView,
    stickyBox: FrameLayout,
    roots: RootAdapter,
    adapter: ExplorerAdapter,
    listener: ExplorerItemBinderActionListener,
) {

    private val holderDelegate = HolderDelegate(recyclerView, stickyBox, roots, adapter, listener)

    fun setComposition(composition: ExplorerItemComposition) = holderDelegate.setComposition(composition)
}