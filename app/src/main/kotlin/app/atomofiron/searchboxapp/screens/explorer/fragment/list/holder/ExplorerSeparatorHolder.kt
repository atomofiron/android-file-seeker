package app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder

import android.view.View
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.databinding.ItemExplorerSeparatorBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootViewHolder.Companion.getTitle

class ExplorerSeparatorHolder(itemView: View) : GeneralHolder<Node>(itemView) {

    private val binding = ItemExplorerSeparatorBinding.bind(itemView)
    private var clickListener: ((Node) -> Unit)? = null

    init {
        itemView.setOnClickListener {
            clickListener?.invoke(item)
        }
    }

    override fun onBind(item: Node, position: Int) {
        binding.title.text = item.getTitle(itemView.resources)
    }

    fun setOnClickListener(listener: (Node) -> Unit) {
        clickListener = listener
    }
}