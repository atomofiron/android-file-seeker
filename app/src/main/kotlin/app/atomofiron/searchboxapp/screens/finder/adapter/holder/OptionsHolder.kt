package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.marginEnd
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginStart
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemSearchOptionsBinding
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.utils.Alpha
import com.google.android.flexbox.FlexboxLayout

class OptionsHolder(
    parent: ViewGroup,
    private val listener: FinderConfigListener
) : GeneralHolder<FinderStateItem>(parent, R.layout.item_search_options) {

    override val hungry = true

    override val item get() = super.item as FinderStateItem.Options

    private val binding = ItemSearchOptionsBinding.bind(itemView)

    init {
        binding.init()
    }

    private fun ItemSearchOptionsBinding.init() {
        root.isFocusable = false
        root.isClickable = false
        root.addOnLayoutChangeListener { _, left, _, right, _, _, _, _, _ ->
            binding.root.onRootLayout(right - left)
        }
        caseSense.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(toggles = it.toggles.copy(ignoreCase = !it.toggles.ignoreCase)) }
        }
        useRegexp.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(toggles = it.toggles.copy(useRegex = !it.toggles.useRegex)) }
        }
        contentSearch.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(toggles = it.toggles.copy(contentSearch = !it.toggles.contentSearch)) }
        }
        excludeDirs.setOnClickListener { view ->
            view as CompoundButton
            update { it.copy(toggles = it.toggles.copy(excludeDirs = !it.toggles.excludeDirs)) }
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

    private fun update(block: (FinderStateItem.Options) -> FinderStateItem.Options) {
        listener.onConfigChange(block(item))
    }

    private fun FlexboxLayout.onRootLayout(width: Int) {
        val children = children
        val leftSpace = children.first().left - paddingLeft
        val rightSpace = width - children.maxOf { it.right } - paddingRight
        val freeSpace = leftSpace + rightSpace
        val center = freeSpace < children.maxOf { it.width + it.marginStart + it.marginEnd }
        when {
            center -> for (line in flexLines) {
                val firstIndex = line.firstIndex
                val lastIndex = firstIndex + line.itemCount.dec()
                val firstLeft = getChildAt(firstIndex).run { left - marginLeft }
                val lastRight = getChildAt(lastIndex).run { right + marginRight }
                val space = firstLeft - paddingLeft + (width - lastRight - paddingRight)
                for (i in firstIndex..lastIndex) {
                    getChildAt(i).translationX = space / 2f
                }
            }
            else -> for (child in children) {
                child.translationX = 0f
            }
        }
    }

    interface FinderConfigListener {
        fun onConfigChange(item: FinderStateItem.Options)
    }
}