package app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder

import android.view.View
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemExplorerBinding
import app.atomofiron.fileseeker.databinding.ItemExplorerSeparatorBinding
import app.atomofiron.searchboxapp.model.explorer.Node

enum class ExplorerItemViewFactory(val layoutId: Int) {
    NodeItem(R.layout.item_explorer) {
        override fun createHolder(itemView: View): ExplorerHolder {
            return ExplorerHolder(itemView)
        }
    },
    OpenedNodeItem(R.layout.item_explorer) {
        override fun createHolder(itemView: View): ExplorerHolder {
            ItemExplorerBinding.bind(itemView).makeOpened()
            return ExplorerHolder(itemView)
        }
    },
    CurrentOpenedNodeItem(R.layout.item_explorer) {
        override fun createHolder(itemView: View): ExplorerHolder {
            ItemExplorerBinding.bind(itemView).makeOpenedCurrent()
            return ExplorerHolder(itemView)
        }
    },
    SeparatorNodeItem(R.layout.item_explorer_separator) {
        override fun createHolder(itemView: View): ExplorerSeparatorHolder {
            ItemExplorerSeparatorBinding.bind(itemView).makeSeparator()
            return ExplorerSeparatorHolder(itemView)
        }
    };

    abstract fun createHolder(itemView: View): GeneralHolder<Node>
}