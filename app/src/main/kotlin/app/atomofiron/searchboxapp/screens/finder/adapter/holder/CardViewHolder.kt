package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.common.util.AppCompatAttr
import app.atomofiron.common.util.isDarkDeep
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.drawable.colorSurfaceContainer
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.utils.colorAttr
import com.google.android.material.card.MaterialCardView

abstract class CardViewHolder<E : FinderStateItem>(
    parent: ViewGroup,
    layoutId: Int,
) : GeneralHolder<E>(wrapWithCard(parent, layoutId)) {
    companion object {
        fun wrapWithCard(parent: ViewGroup, id: Int): View {
            val inflater = LayoutInflater.from(parent.context)
            val cardView = inflater.inflate(R.layout.item_card_container, parent, false) as MaterialCardView
            if (cardView.context.isDarkDeep()) {
                cardView.setCardBackgroundColor(cardView.context.colorSurfaceContainer())
            }
            cardView.strokeWidth = parent.resources.getDimensionPixelSize(R.dimen.stroke_width)
            inflater.inflate(id, cardView, true)
            return cardView
        }
    }

    protected val view: View = (itemView as ViewGroup).getChildAt(0)

    init {
        setSelected(false)
    }

    fun setSelected(yes: Boolean) {
        (itemView as MaterialCardView).strokeColor = when {
            yes -> context.colorAttr(AppCompatAttr.colorPrimary)
            else -> Color.TRANSPARENT
        }
    }
}