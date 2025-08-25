package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.graphics.Outline
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.recyclerview.widget.GridLayoutManager
import app.atomofiron.common.recycler.GeneralAdapter
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemSearchEditOptionsBinding
import app.atomofiron.searchboxapp.custom.view.layout.MeasuringRecyclerView
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.utils.Alpha
import com.google.android.material.chip.Chip
import kotlin.math.max
import kotlin.math.min

class EditOptionsHolder(
    parent: ViewGroup,
    private val listener: FinderConfigListener,
    private val isLocal: Boolean,
) : GeneralHolder<FinderStateItem>(parent, R.layout.item_search_edit_options) {

    override val hungry = true

    override val item get() = super.item as FinderStateItem.EditOptions

    private val optionMinWidth = parent.resources.getDimensionPixelSize(R.dimen.option_width)
    private val binding = ItemSearchEditOptionsBinding.bind(itemView)
    private val recyclerView = itemView as MeasuringRecyclerView
    private val adapter = Adapter(::onClick)
    private val layoutManager = GridLayoutManager(parent.context, 1, GridLayoutManager.HORIZONTAL, false)

    init {
        recyclerView.outlineProvider = Clipping(parent.resources.getDimension(R.dimen.corner_extra))
        recyclerView.clipToOutline = true
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
        recyclerView.addMeasureListener { width, _ ->
            val available = recyclerView.run { width - paddingStart - paddingEnd }
            layoutManager.spanCount = max(1, adapter.itemCount / max(1, available / optionMinWidth))
        }
        recyclerView.addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
            val first = recyclerView.getChildAt(0)
            val last = recyclerView.getChildAt(recyclerView.childCount.dec())
            var left = min(first.left, first.right)
            left = min(left, last.left)
            left = min(left, last.right)
            var right = max(first.left, first.right)
            right = max(right, last.left)
            right = max(right, last.right)
            var dx = (view.width - right - view.paddingRight - first.marginRight) - (left - view.paddingLeft - first.marginLeft)
            if (dx > optionMinWidth) dx = 0
            recyclerView.translationX = dx / 2f
        }
    }

    override fun onBind(item: FinderStateItem, position: Int) = binding.run {
        item as FinderStateItem.EditOptions

        val items = mutableListOf(Item(!item.toggles.ignoreCase), Item(item.toggles.useRegex))
        if (!isLocal) {
            items.add(Item(isChecked = item.contentSearch))
            items.add(Item(isChecked = item.excludeDirs, isEnabled = !item.excludeDirs || !item.contentSearch))
        }
        adapter.submit(items)
    }

    private fun onClick(position: Int, toChecked: Boolean) {
        var options = item.toggles
        options = when (position) {
            0 -> options.copy(ignoreCase = !toChecked)
            1 -> options.copy(useRegex = toChecked)
            2 -> options.copy(contentSearch = toChecked)
            else -> options.copy(excludeDirs = toChecked)
        }
        listener.onConfigChange(options)
    }

    private class Clipping(private val radius: Float) : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) = outline.setRoundRect(0, -radius.toInt(), view.width, view.height, radius)
    }

    interface FinderConfigListener {
        fun onConfigChange(options: SearchOptions)
    }
}

private data class Item(
    val isChecked: Boolean,
    val isEnabled: Boolean = true,
)

private class Adapter(private val listener: (Int, Boolean) -> Unit) : GeneralAdapter<Item, GeneralHolder<Item>>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): GeneralHolder<Item> {
        val chip = inflater.inflate(R.layout.item_search_option, parent, false)
        val holder = Holder(chip)
        chip.setOnClickListener {
            val item = get(holder.bindingAdapterPosition)
            listener(holder.bindingAdapterPosition, !item.isChecked)
        }
        return holder
    }
}

private class Holder(view: View) : GeneralHolder<Item>(view) {

    private val chip = view as Chip

    override fun onBind(item: Item, position: Int) {
        chip.setText(getStringId())
        chip.isChecked = item.isChecked
        chip.isEnabled = item.isEnabled
        chip.setChipIconResource(getIconId())
        chip.chipIcon?.alpha = Alpha.enabledInt(item.isEnabled)
    }

    private fun getStringId(): Int = when (bindingAdapterPosition) {
        0 -> R.string.case_sens
        1 -> R.string.use_regular_expression
        2 -> R.string.search_in_the_content
        else -> R.string.pref_exclude_dirs
    }

    private fun getIconId(): Int = when (bindingAdapterPosition) {
        0 -> R.drawable.ic_config_case
        1 -> R.drawable.ic_config_regex
        2 -> R.drawable.ic_config_text
        else -> R.drawable.ic_config_folder
    }
}
