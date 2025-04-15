package app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder

import android.view.View
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemExplorerBinding
import app.atomofiron.fileseeker.databinding.ItemExplorerSeparatorBinding
import app.atomofiron.searchboxapp.model.explorer.Node

enum class ExplorerItemViewFactory(
    val viewType: Int,
    val layoutId: Int,
    val cache: Int = 4,
) {
    NodeItem(0, R.layout.item_explorer, cache = 32) {
        override fun createHolder(itemView: View): ExplorerHolder {
            return ExplorerHolder(itemView)
        }
    },
    OpenedNodeItem(1, R.layout.item_explorer) {
        override fun createHolder(itemView: View): ExplorerHolder {
            ItemExplorerBinding.bind(itemView).makeOpened()
            return ExplorerHolder(itemView)
        }
    },
    CurrentOpenedNodeItem(2, R.layout.item_explorer) {
        override fun createHolder(itemView: View): ExplorerHolder {
            ItemExplorerBinding.bind(itemView).makeOpenedCurrent()
            return ExplorerHolder(itemView)
        }
    },
    SeparatorNodeItem(3, R.layout.item_explorer_separator) {
        override fun createHolder(itemView: View): ExplorerSeparatorHolder {
            ItemExplorerSeparatorBinding.bind(itemView).makeSeparator()
            return ExplorerSeparatorHolder(itemView)
        }
    };

    abstract fun createHolder(itemView: View): GeneralHolder<Node>
}