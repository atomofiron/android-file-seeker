package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.marginEnd
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginStart
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemSearchEditOptionsBinding
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.toInt
import com.google.android.flexbox.FlexboxLayout
import kotlin.math.max
import kotlin.math.min

class EditOptionsHolder(
    parent: ViewGroup,
    private val listener: FinderConfigListener,
) : GeneralHolder<FinderStateItem>(parent, R.layout.item_search_edit_options) {

    override val hungry = true

    override val item get() = super.item as FinderStateItem.EditOptions

    private val binding = ItemSearchEditOptionsBinding.bind(itemView)

    init {
        binding.init()
    }

    private fun ItemSearchEditOptionsBinding.init() {
        root.isFocusable = false
        root.isClickable = false
        root.addOnLayoutChangeListener { _, left, _, right, _, _, _, _, _ ->
            binding.root.onRootLayout(right - left)
        }
        caseSense.setOnClickListener {
            update { it.copy(ignoreCase = !it.ignoreCase) }
        }
        useRegexp.setOnClickListener {
            update { it.copy(useRegex = !it.useRegex) }
        }
        contentSearch.setOnClickListener {
            update { it.copy(contentSearch = !it.contentSearch) }
        }
        excludeDirs.setOnClickListener {
            update { it.copy(excludeDirs = !it.excludeDirs) }
        }
    }

    override fun onBind(item: FinderStateItem, position: Int) = binding.run {
        item as FinderStateItem.EditOptions

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
                var minLeft = 0
                var maxRight = 0
                getPresentedChildAt(firstIndex, lastIndex)?.let {
                    minLeft = it.left - it.marginLeft
                    maxRight = it.right + it.marginRight
                } ?: continue
                getPresentedChildAt(lastIndex, firstIndex)?.let {
                    minLeft = min(minLeft, it.left - it.marginLeft)
                    maxRight = max(maxRight, it.right + it.marginRight)
                }
                val space = minLeft - paddingLeft + (width - maxRight - paddingRight)
                for (i in firstIndex..lastIndex) {
                    getChildAt(i).translationX = space / 2f
                }
            }
            else -> for (child in children) {
                child.translationX = 0f
            }
        }
    }

    private fun ViewGroup.getPresentedChildAt(from: Int, to: Int): View? {
        var index = from
        val step = (to > from).toInt()
        while (true) {
            val view = getChildAt(index)
            when (view?.isGone) {
                null -> return null
                false -> return view
                true -> when (index) {
                    to -> return null
                    else -> index += step
                }
            }
        }
    }

    interface FinderConfigListener {
        fun onConfigChange(options: SearchOptions)
    }
}