package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import android.widget.TextView
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem

class TitleHolder(parent: ViewGroup) : GeneralHolder<FinderStateItem>(parent, R.layout.item_finder_title) {

    override val hungry = true

    private val tvTitle = itemView.findViewById<TextView>(R.id.finder_tv_tip)

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.Title
        tvTitle.setText(item.stringId)
    }
}