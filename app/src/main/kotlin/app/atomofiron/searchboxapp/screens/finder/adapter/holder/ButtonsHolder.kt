package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import androidx.core.view.isVisible
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemFinderButtonsBinding
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem

class ButtonsHolder(
    parent: ViewGroup,
    private val listener: FinderButtonsListener
) : GeneralHolder<FinderStateItem.Buttons>(parent, R.layout.item_finder_buttons) {

    override val hungry = false

    private val binding = ItemFinderButtonsBinding.bind(itemView)

    init {
        binding.history.setOnClickListener {
            listener.onHistoryClick()
        }
        binding.test.setOnClickListener {
            listener.onTestClick()
        }
        binding.options.setOnClickListener {
            listener.onOptionsVisibilityClick()
        }
    }

    override fun minWidth(): Float = itemView.resources.getDimension(R.dimen.finder_buttons)

    override fun onBind(item: FinderStateItem.Buttons, position: Int) {
        binding.test.isVisible = item.withTest
    }

    interface FinderButtonsListener {
        fun onHistoryClick()
        fun onTestClick()
        fun onOptionsVisibilityClick()
    }
}