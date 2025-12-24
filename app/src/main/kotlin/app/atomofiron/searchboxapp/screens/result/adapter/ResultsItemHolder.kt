package app.atomofiron.searchboxapp.screens.result.adapter

import androidx.core.view.isVisible
import app.atomofiron.fileseeker.databinding.ItemExplorerBinding
import app.atomofiron.fileseeker.databinding.ItemResultCountBinding
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinder
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinder.ExplorerItemBinderActionListener
import app.atomofiron.searchboxapp.utils.attach

class ResultsItemHolder(binding: ItemExplorerBinding) : ResultsHolder<ResultItem.Item>(binding.root) {

    private val binder = ExplorerItemBinder(binding)

    private val tvCounter = binding.root.attach(ItemResultCountBinding::inflate).resultTvCount

    fun setOnItemActionListener(listener: ExplorerItemBinderActionListener?) {
        binder.setOnItemActionListener(listener)
    }

    override fun onBind(item: ResultItem.Item, position: Int) {
        item.item.let { binder.bind(it) }
        tvCounter.isVisible = item.match.withCounter
        tvCounter.text = item.match.count.toString()
    }

    fun bindComposition(composition: ExplorerItemComposition) = binder.bindComposition(composition)
}