package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import android.widget.Button
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

class ButtonsHolder(
    parent: ViewGroup,
    layoutId: Int,
    private val listener: FinderButtonsListener
) : GeneralHolder<FinderStateItem>(parent, layoutId) {
    private val btnHistory = itemView.findViewById<Button>(R.id.item_config_history)
    private val btnVisibility = itemView.findViewById<Button>(R.id.item_config_visibility)

    init {
        btnHistory.setOnClickListener {
            listener.onHistoryClick()
        }
        btnVisibility.setOnClickListener {
            listener.onConfigVisibilityClick()
        }
    }

    override fun onBind(item: FinderStateItem, position: Int) = Unit

    interface FinderButtonsListener {
        fun onHistoryClick()
        fun onConfigVisibilityClick()
    }
}