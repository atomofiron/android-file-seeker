package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemTextFieldBinding
import app.atomofiron.searchboxapp.custom.view.TextField
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem

class EditMaxDepthHolder(
    parent: ViewGroup,
    private val output: OnEditMaxDepthListener,
) : GeneralHolder<FinderStateItem>(parent, R.layout.item_text_field), TextField.OnSubmitListener {

    override val hungry = true

    private val binding = ItemTextFieldBinding.bind(itemView)
    private val delegate = TextFieldHolderDelegate(binding)

    init {
        binding.box.setHint(R.string.pref_max_depth)
        binding.field.maxLines = 1
        binding.field.addOnSubmitListener(this)
    }

    override fun minWidth(): Float = delegate.minWidth()

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.MaxDepth
        binding.field.setText(item.value.toString())
    }

    override fun onSubmit(value: String) = output.onEditMaxDepth(value.toInt())

    interface OnEditMaxDepthListener {
        fun onEditMaxDepth(new: Int)
    }
}