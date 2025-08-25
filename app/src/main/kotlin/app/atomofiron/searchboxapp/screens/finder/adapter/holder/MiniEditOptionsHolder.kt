package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.view.isGone
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemSearchEditOptionsMiniBinding
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditOptionsHolder.FinderConfigListener
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.utils.Alpha

class MiniEditOptionsHolder(
    parent: ViewGroup,
    private val listener: FinderConfigListener,
) : GeneralHolder<FinderStateItem>(parent, R.layout.item_search_edit_options_mini) {

    override val hungry = true

    override val item get() = super.item as FinderStateItem.Options

    private val binding = ItemSearchEditOptionsMiniBinding.bind(itemView)

    init {
        binding.init()
    }

    private fun ItemSearchEditOptionsMiniBinding.init() {
        root.isFocusable = false
        root.isClickable = false
        caseSense.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(ignoreCase = !it.ignoreCase) }
        }
        useRegexp.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(useRegex = !it.useRegex) }
        }
        contentSearch.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(contentSearch = !it.contentSearch) }
        }
        excludeDirs.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(excludeDirs = !it.excludeDirs) }
        }
    }

    override fun onBind(item: FinderStateItem, position: Int) = binding.run {
        item as FinderStateItem.Options

        contentSearch.isGone = item.isLocal
        excludeDirs.isGone = item.isLocal

        caseSense.isChecked = !item.ignoreCase
        useRegexp.isChecked = item.useRegex
        contentSearch.isChecked = item.contentSearch
        excludeDirs.isChecked = item.excludeDirs
        excludeDirs.isEnabled = !item.contentSearch
        excludeDirs.chipIcon?.alpha = Alpha.enabledInt(!item.excludeDirs || !item.contentSearch)
    }

    private fun update(block: (SearchOptions) -> SearchOptions) {
        listener.onConfigChange(block(item.toggles))
    }
}
