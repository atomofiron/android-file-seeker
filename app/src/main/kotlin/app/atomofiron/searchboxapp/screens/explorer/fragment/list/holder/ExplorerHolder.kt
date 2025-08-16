package app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder

import android.graphics.drawable.RippleDrawable
import android.view.View
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.ExplorerItemActionListener
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl
import app.atomofiron.searchboxapp.utils.Const

class ExplorerHolder(itemView: View) : GeneralHolder<Node>(itemView) {

    private val binder = ExplorerItemBinderImpl(itemView)

    override fun onBind(item: Node, position: Int) = binder.bind(item)

    fun setOnItemActionListener(listener: ExplorerItemActionListener?) = binder.setOnItemActionListener(listener)

    fun bindComposition(composition: ExplorerItemComposition) = binder.bindComposition(composition)

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
        }, Const.COMMON_DELAY)
    }
}