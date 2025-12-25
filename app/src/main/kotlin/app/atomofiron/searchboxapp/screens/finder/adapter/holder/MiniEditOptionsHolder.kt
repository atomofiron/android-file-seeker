package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation
import android.view.ViewGroup
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemSearchEditOptionsMiniBinding
import app.atomofiron.searchboxapp.custom.drawable.colorSurfaceContainer
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditOptionsHolder.FinderConfigListener
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.utils.Alpha

class MiniEditOptionsHolder(
    parent: ViewGroup,
    private val listener: FinderConfigListener,
) : GeneralHolder<FinderStateItem.Options>(parent, R.layout.item_search_edit_options_mini) {

    override val hungry = true
    private val corners = resources.getDimension(R.dimen.corner_extra)

    private val binding = ItemSearchEditOptionsMiniBinding.bind(itemView)

    init {
        binding.init()
    }

    private fun ItemSearchEditOptionsMiniBinding.init() {
        root.isFocusable = false
        root.isClickable = false
        val color = context.colorSurfaceContainer()
        toggles.background = GradientDrawable(Orientation.BOTTOM_TOP, intArrayOf(color, color))
            .apply { cornerRadius = corners }
        caseSense.setOnClickListener {
            update { it.edit(ignoreCase = !it.ignoreCase) }
        }
        useRegexp.setOnClickListener {
            update { it.edit(regex = !it.regex) }
        }
        contentSearch.setOnClickListener {
            update { it.edit(contentSearch = !it.contentSearch) }
        }
        excludeDirs.setOnClickListener {
            update { it.edit(excludeDirs = !it.excludeDirs) }
        }
    }

    override fun onBind(item: FinderStateItem.Options, position: Int) = binding.run {
        caseSense.isChecked = !item.ignoreCase
        useRegexp.isChecked = item.regex
        contentSearch.isChecked = item.contentSearch
        excludeDirs.isChecked = item.excludeDirs
        excludeDirs.isEnabled = !item.contentSearch
        excludeDirs.chipIcon?.alpha = Alpha.enabledInt(!item.excludeDirs || !item.contentSearch)
    }

    private fun update(block: (SearchOptions) -> SearchOptions) = listener.onOptionsChange(block(item.toggles))
}
