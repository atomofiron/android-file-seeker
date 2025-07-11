package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemTextFieldBinding
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.utils.ByteSizeDelegate.Companion.makeByteSize

class EditMaxSizeHolder(
    parent: ViewGroup,
    private val output: OnEditMaxSizeListener,
) : GeneralHolder<FinderStateItem>(parent, R.layout.item_text_field) {

    override val hungry = true

    private val binding = ItemTextFieldBinding.bind(itemView)
    private val delegate = TextFieldHolderDelegate(binding)

    init {
        binding.box.setHint(R.string.pref_max_size)
        binding.field.maxLines = 1
        binding.field.makeByteSize { output.onEditMaxSize(it) }
    }

    override fun minWidth(): Float = delegate.minWidth()

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.MaxSize
        binding.field.setText(item.value.toString())
    }

    interface OnEditMaxSizeListener {
        fun onEditMaxSize(new: Int)
    }
}
