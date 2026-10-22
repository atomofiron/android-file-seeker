package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.finder.SelectableCharset
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.updateMarginLayoutParams
import com.google.android.material.chip.Chip

class CharsetHolder(
    parent: ViewGroup,
    private val output: CharsetsHolder.CharsetsOutput,
) : GeneralHolder<SelectableCharset>(parent, R.layout.chip) {

    private val chipView = itemView.findViewById<Chip>(R.id.chip)

    init {
        itemView.isClickable = false
        itemView.isFocusable = false
        chipView.setOnClickListener { output.onCharsetClick(item.name, !chipView.isSelected) }
        chipView.updateMarginLayoutParams {
            marginStart = resources.getDimensionPixelSize(R.dimen.padding_mini)
            marginEnd = marginStart
        }
    }

    override fun onBind(item: SelectableCharset, position: Int) {
        chipView.text = item.name
        chipView.isSelected = item.selected
        chipView.isEnabled = item.enabled
        chipView.alpha = Alpha.enabled(item.enabled)
    }
}