package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

class DisclaimerHolder(
    parent: ViewGroup,
    layoutId: Int,
) : GeneralHolder<FinderStateItem>(parent, layoutId) {

    override fun onBind(item: FinderStateItem, position: Int) = Unit
}