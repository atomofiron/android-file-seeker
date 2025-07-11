package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem

class DisclaimerHolder(parent: ViewGroup) : CardViewHolder(parent, R.layout.item_finder_disclaimer) {

    override val hungry = false

    init {
        itemView.foreground = null
    }

    override fun minWidth(): Float = itemView.resources.getDimension(R.dimen.finder_disclaimer)

    override fun onBind(item: FinderStateItem, position: Int) = Unit
}