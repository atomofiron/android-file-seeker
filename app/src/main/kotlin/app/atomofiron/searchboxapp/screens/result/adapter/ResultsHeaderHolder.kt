package app.atomofiron.searchboxapp.screens.result.adapter

import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemHeaderBinding

class ResultsHeaderHolder(private val binding: ItemHeaderBinding) : ResultsHolder(binding.root) {

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
        binding.itemTvTitle.text = string.toString()
    }
}