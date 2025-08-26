package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.atomofiron.fileseeker.R
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.common.util.isDarkDeep
import app.atomofiron.searchboxapp.custom.drawable.colorSurfaceContainer
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import com.google.android.material.card.MaterialCardView

abstract class CardViewHolder(
    parent: ViewGroup,
    layoutId: Int,
) : GeneralHolder<FinderStateItem>(wrapWithCard(parent, layoutId)) {
    companion object {
        fun wrapWithCard(parent: ViewGroup, id: Int): View {
            val inflater = LayoutInflater.from(parent.context)
            val cardView = inflater.inflate(R.layout.item_card_container, parent, false) as MaterialCardView
            if (cardView.context.isDarkDeep()) {
                cardView.setCardBackgroundColor(cardView.context.colorSurfaceContainer())
            }
            inflater.inflate(id, cardView, true)
            return cardView
        }
    }

    protected val view: View = (itemView as ViewGroup).getChildAt(0)
}