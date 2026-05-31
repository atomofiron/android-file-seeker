package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemTextFieldBinding
import app.atomofiron.searchboxapp.custom.drawable.makeHoled
import app.atomofiron.searchboxapp.model.other.ByteSize
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.utils.makeByteSize

class EditMaxSizeHolder(
    parent: ViewGroup,
    private val output: OnEditMaxSizeListener,
) : GeneralHolder<FinderStateItem.MaxSize>(parent, R.layout.item_text_field) {

    override val hungry = true

    private val binding = ItemTextFieldBinding.bind(itemView)
    private val delegate = TextFieldHolderDelegate(binding)

    init {
        binding.box.setHint(R.string.pref_max_size)
        binding.field.maxLines = 1
        binding.field.makeByteSize { output.onEditMaxSize(it) }
        binding.field.makeHoled(binding.box)
    }

    override fun minWidth(): Float = delegate.minWidth()

    override fun onBind(item: FinderStateItem.MaxSize, position: Int) {
        binding.field.setText(item.value.toString())
        binding.box.isEnabled = item.enabled
    }

    interface OnEditMaxSizeListener {
        fun onEditMaxSize(new: ByteSize)
    }
}
