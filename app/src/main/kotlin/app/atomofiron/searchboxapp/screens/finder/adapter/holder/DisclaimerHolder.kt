package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem

class DisclaimerHolder(
    parent: ViewGroup,
    layoutId: Int,
) : CardViewHolder(parent, layoutId) {

    init {
        itemView.foreground = null
    }

    override fun onBind(item: FinderStateItem, position: Int) = Unit
}