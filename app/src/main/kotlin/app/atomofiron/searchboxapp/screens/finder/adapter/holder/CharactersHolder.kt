package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import app.atomofiron.fileseeker.R
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem

class CharactersHolder(
    parent: ViewGroup,
    layoutId: Int,
    private val listener: OnActionListener
) : GeneralHolder<FinderStateItem>(parent, layoutId), View.OnClickListener {
    private var currentItem: FinderStateItem.SpecialCharactersItem? = null

    override fun onBind(item: FinderStateItem, position: Int) {
        if (currentItem == item) {
            return
        }
        item as FinderStateItem.SpecialCharactersItem
        currentItem = item
        val itemView = itemView
        itemView as ViewGroup
        itemView.removeAllViews()

        if (item.characters.isNotEmpty() && item.characters[0].isNotEmpty()) {
            for (c in item.characters) {
                val inflater = LayoutInflater.from(itemView.context)
                val view = inflater.inflate(R.layout.button_character, itemView, false) as Button
                view.text = c
                view.setOnClickListener(this)
                itemView.addView(view)
            }
        }
    }

    override fun onClick(v: View) {
        v as Button
        listener.onCharacterClick(v.text.toString())
    }

    interface OnActionListener {
        fun onCharacterClick(value: String)
    }
}