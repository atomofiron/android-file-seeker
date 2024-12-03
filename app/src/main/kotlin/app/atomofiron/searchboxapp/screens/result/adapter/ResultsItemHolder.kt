package app.atomofiron.searchboxapp.screens.result.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl

class ResultsItemHolder(itemView: View) : ResultsHolder(itemView) {
    private val binder = ExplorerItemBinderImpl(itemView)

    private val tvCounter = LayoutInflater
        .from(itemView.context)
        .inflate(R.layout.item_result_count, itemView as ViewGroup)
        .findViewById<TextView>(R.id.result_tv_count)

    fun setOnItemActionListener(listener: ExplorerItemBinderImpl.ExplorerItemBinderActionListener?) {
        binder.onItemActionListener = listener
    }

    override fun onBind(item: ResultItem, position: Int) {
        item as ResultItem.Item
        val result = item.item
        binder.onBind(result.item)
        tvCounter.isVisible = result.withCounter
        tvCounter.text = result.count.toString()
    }

    fun bindComposition(composition: ExplorerItemComposition) = binder.bindComposition(composition)
}