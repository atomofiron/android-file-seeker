package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import android.widget.Button
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem

class ButtonsHolder(
    parent: ViewGroup,
    private val listener: FinderButtonsListener
) : GeneralHolder<FinderStateItem>(parent, R.layout.item_finder_buttons) {

    override val hungry = false

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

    override fun minWidth(): Float = itemView.resources.getDimension(R.dimen.finder_buttons)

    override fun onBind(item: FinderStateItem, position: Int) = Unit

    interface FinderButtonsListener {
        fun onHistoryClick()
        fun onConfigVisibilityClick()
    }
}