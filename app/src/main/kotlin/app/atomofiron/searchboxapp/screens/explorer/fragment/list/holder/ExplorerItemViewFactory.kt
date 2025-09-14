package app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder

import android.view.View
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.Node

enum class ExplorerItemViewFactory(
    val viewType: Int,
    val layoutId: Int,
    val cache: Int = 4,
) {
    Regular(0, R.layout.item_explorer, cache = 32) {
        override fun createHolder(itemView: View) = ExplorerHolder(itemView, isOpened = false)
    },
    Opened(1, R.layout.item_explorer) {
        override fun createHolder(itemView: View) = ExplorerHolder(itemView, isOpened = true)
    },
    Separator(2, R.layout.item_explorer_separator) {
        override fun createHolder(itemView: View) = ExplorerSeparatorHolder(itemView)
    },
    ;
    abstract fun createHolder(itemView: View): GeneralHolder<Node>
}