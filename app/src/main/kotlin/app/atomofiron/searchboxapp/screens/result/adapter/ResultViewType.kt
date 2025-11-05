package app.atomofiron.searchboxapp.screens.result.adapter;

import android.view.LayoutInflater
import android.view.ViewGroup
import app.atomofiron.fileseeker.databinding.ItemExplorerBinding
import app.atomofiron.fileseeker.databinding.ItemHeaderBinding

enum class ResultViewType(val viewType: Int) {
    Header(0),
    Item(1),
    ;
    fun createHolder(parent: ViewGroup, inflater: LayoutInflater): ResultsHolder<out ResultItem> = when (this) {
        Header -> ResultsHeaderHolder(ItemHeaderBinding.inflate(inflater, parent, false))
        Item -> ResultsItemHolder(ItemExplorerBinding.inflate(inflater, parent, false))
    }
    companion object {
        operator fun invoke(viewType: Int) = entries.find { it.viewType == viewType }!!
    }
}
