package app.atomofiron.searchboxapp.screens.result.adapter

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemHeaderBinding
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.colorAttr

class ResultsHeaderHolder(private val binding: ItemHeaderBinding) : ResultsHolder<ResultItem.Header>(binding.root) {

    init {
        binding.root.setOnClickListener {
            listener.onErrorsClick()
        }
    }

    override fun onBind(item: ResultItem.Header, position: Int) {
        val string = StringBuilder()
        if (item.dirCount > 0) {
            string.append(context.resources.getQuantityString(R.plurals.x_dirs, item.dirCount, item.dirCount))
        }
        if (item.dirCount > 0 && item.fileCount > 0) {
            string.append(", ")
        }
        if (item.fileCount > 0) {
            string.append(context.resources.getQuantityString(R.plurals.x_files, item.fileCount, item.fileCount))
        }
        if (item.dirCount > 0 || item.fileCount > 0) {
            string.append(", ")
        }
        binding.title.text = if (item.errorCount > 0) {
            val errors = context.resources.getQuantityString(R.plurals.x_errors, item.errorCount, item.errorCount)
            string.append(errors)
            string.append(Const.NBSP)
            val start = string.length
            string.append(context.resources.getString(R.string.show))
            SpannableString(string).apply {
                val span = ForegroundColorSpan(context.colorAttr(MaterialAttr.colorPrimary))
                setSpan(span, start, length, 0)
                setSpan(StyleSpan(Typeface.BOLD), 0, start, 0)
            }
        } else {
            string.toString()
        }
        binding.root.isClickable = item.errorCount > 0
    }
}