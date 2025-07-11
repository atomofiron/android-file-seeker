package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.SpecialCharacters

class CharactersHolder(
    parent: ViewGroup,
    private val listener: OnActionListener
) : GeneralHolder<FinderStateItem>(parent, R.layout.item_characters), View.OnClickListener {

    override val hungry = false

    override fun minWidth(): Float = itemView.resources.run {
        (itemOrNull as SpecialCharacters?)
            ?.characters
            ?.size
            ?.let { it * getDimension(R.dimen.finder_char) }
            ?: getDimension(R.dimen.finder_query_field)
    }

    override fun onBind(item: FinderStateItem, position: Int) {
        item as SpecialCharacters
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