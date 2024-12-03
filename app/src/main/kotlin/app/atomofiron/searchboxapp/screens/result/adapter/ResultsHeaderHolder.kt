package app.atomofiron.searchboxapp.screens.result.adapter

import android.view.View
import android.widget.TextView
import app.atomofiron.searchboxapp.R

class ResultsHeaderHolder(itemView: View) : ResultsHolder(itemView) {
    private val tvTitle = itemView.findViewById<TextView>(R.id.item_tv_title)

    override fun onBind(item: ResultItem, position: Int) {
        item as ResultItem.Header

        val string = StringBuilder()
        if (item.dirsCount > 0) {
            string.append(context.resources.getQuantityString(R.plurals.x_dirs, item.dirsCount, item.dirsCount))
        }
        if (item.dirsCount > 0 && item.filesCount > 0) {
            string.append(", ")
        }
        if (item.filesCount > 0) {
            string.append(context.resources.getQuantityString(R.plurals.x_files, item.filesCount, item.filesCount))
        }
        tvTitle.text = string.toString()
    }
}