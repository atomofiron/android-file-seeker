package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.common.util.extension.indexOfFirst
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemChipsBinding
import app.atomofiron.searchboxapp.model.finder.SelectableCharset
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.addOnAttachListener
import app.atomofiron.searchboxapp.utils.delayed
import app.atomofiron.searchboxapp.utils.onResize
import app.atomofiron.searchboxapp.utils.scope

class CharsetsHolder : GeneralHolder<FinderStateItem.Charsets> {

    override val hungry = true

    private val binding = ItemChipsBinding.bind(itemView)
    private val adapter: Adapter
    private val scope by itemView.scope()
    private val scrollToSelected = delayed(Const.COMMON_DELAY) {
        adapter.currentList
            .indexOfFirst { it.selected }
            .takeIf { it >= 0 }
            ?.let { binding.root.smoothScrollToPosition(it) }
    }

    constructor(output: CharsetsOutput, binding: ItemChipsBinding) : super(binding.root) {
        adapter = Adapter(output)
        binding.root.adapter = adapter
    }

    constructor(parent: ViewGroup, output: CharsetsOutput) : super(parent, R.layout.item_chips) {
        adapter = Adapter(output)
        binding.root.adapter = adapter
    }

    init {
        binding.root.run {
            itemAnimator = null
            onResize { scrollToSelected(scope) }
            addOnAttachListener { scrollToSelected(scope) }
        }
    }

    override fun onBind(item: FinderStateItem.Charsets, position: Int) = bind(item.charsets)

    fun bind(charsets: List<SelectableCharset>) {
        adapter.submitList(charsets)
        scrollToSelected(scope)
    }

    private class Adapter(private val output: CharsetsOutput): ListAdapter<SelectableCharset, CharsetHolder>(DiffUtilCallback) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CharsetHolder(parent, output)
        override fun onBindViewHolder(holder: CharsetHolder, position: Int) = holder.bind(currentList[position], position)
    }

    private object DiffUtilCallback : DiffUtil.ItemCallback<SelectableCharset>() {
        override fun areItemsTheSame(oldItem: SelectableCharset, newItem: SelectableCharset): Boolean = oldItem.name == newItem.name
        override fun areContentsTheSame(oldItem: SelectableCharset, newItem: SelectableCharset): Boolean = oldItem == newItem
    }

    interface CharsetsOutput {
        fun onCharsetClick(charset: String, select: Boolean) = Unit
    }
}