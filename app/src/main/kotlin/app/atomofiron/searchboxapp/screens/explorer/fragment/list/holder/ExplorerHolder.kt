package app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder

import android.graphics.drawable.RippleDrawable
import android.view.View
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinder
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl

class ExplorerHolder(itemView: View) : GeneralHolder<Node>(itemView),
    ExplorerItemBinder by ExplorerItemBinderImpl(itemView) {

    override fun onBind(item: Node, position: Int) = onBind(item)

    fun highlight() {
        val background = itemView.background as RippleDrawable
        val normalState = background.state
        if (normalState.contains(android.R.attr.state_pressed)) return
        val pressedState = normalState.toMutableList().apply {
            add(android.R.attr.state_pressed)
        }.toIntArray()
        background.state = pressedState
        itemView.postDelayed({
            background.state = normalState
        }, 250)
    }
}