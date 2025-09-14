package app.atomofiron.searchboxapp.screens.result.adapter

import androidx.core.view.isVisible
import app.atomofiron.fileseeker.databinding.ItemExplorerBinding
import app.atomofiron.fileseeker.databinding.ItemResultCountBinding
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl.ExplorerItemBinderActionListener
import app.atomofiron.searchboxapp.utils.attach

class ResultsItemHolder(binding: ItemExplorerBinding) : ResultsHolder(binding.root) {

    private val binder = ExplorerItemBinderImpl(binding)

    private val tvCounter = binding.root.attach(ItemResultCountBinding::inflate).resultTvCount

    fun setOnItemActionListener(listener: ExplorerItemBinderActionListener?) {
        binder.setOnItemActionListener(listener)
    }

    override fun onBind(item: ResultItem, position: Int) {
        item as ResultItem.Item
        val result = item.item
        binder.bind(result.item)
        tvCounter.isVisible = result.withCounter
        tvCounter.text = result.count.toString()
    }

    fun bindComposition(composition: ExplorerItemComposition) = binder.bindComposition(composition)
}