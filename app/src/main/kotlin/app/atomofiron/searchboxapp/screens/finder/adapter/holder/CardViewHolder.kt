package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.atomofiron.searchboxapp.R
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

abstract class CardViewHolder(parent: ViewGroup, layoutId: Int)
    : GeneralHolder<FinderStateItem>(wrapWithCard(parent, layoutId)) {
    companion object {
        fun wrapWithCard(parent: ViewGroup, id: Int): View {
            val inflater = LayoutInflater.from(parent.context)
            val cardView = inflater.inflate(R.layout.item_card_container, parent, false) as ViewGroup
            inflater.inflate(id, cardView, true)
            return cardView
        }
    }
}