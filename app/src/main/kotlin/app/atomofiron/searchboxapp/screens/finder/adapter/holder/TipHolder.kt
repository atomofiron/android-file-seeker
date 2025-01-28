package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import android.widget.TextView
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.screens.finder.model.FinderStateItem

class TipHolder(parent: ViewGroup, layoutId: Int) : GeneralHolder<FinderStateItem>(parent, layoutId) {
    private val tvTitle = itemView.findViewById<TextView>(R.id.finder_tv_tip)

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.TipItem
        tvTitle.setText(item.titleId)
    }
}