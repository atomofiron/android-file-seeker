package app.atomofiron.searchboxapp.screens.result.adapter

import androidx.recyclerview.widget.DiffUtil

class ResultDiffUtilCallback(
    private val old: List<ResultItem>,
    private val new: List<ResultItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areItemsTheSame(i: Int, j: Int): Boolean = old[i].uniqueId == new[j].uniqueId

    override fun areContentsTheSame(i: Int, j: Int): Boolean = old[i] == new[j]
}