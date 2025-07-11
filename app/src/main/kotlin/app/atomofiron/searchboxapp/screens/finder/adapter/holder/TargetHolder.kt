package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import androidx.core.content.ContextCompat
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.getIcon
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootViewHolder.Companion.getTitle
import com.google.android.material.chip.Chip

class TargetHolder(
    parent: ViewGroup,
    layoutId: Int,
    private val output: TargetsHolder.FinderTargetsOutput,
) : GeneralHolder<Node>(parent, layoutId) {

    private val chipView = itemView.findViewById<Chip>(R.id.item_chip_target)

    init {
        itemView.isClickable = false
        itemView.isFocusable = false
        chipView.setOnClickListener { output.onTargetClick(item, !chipView.isSelected) }
    }

    override fun minWidth(): Float = throw UnsupportedOperationException()

    override fun onBind(item: Node, position: Int) {
        val icon = ContextCompat.getDrawable(context, item.getIcon())
        chipView.chipIcon = icon
        chipView.text = item.getTitle(itemView.resources)
        chipView.isSelected = item.isChecked
    }
}