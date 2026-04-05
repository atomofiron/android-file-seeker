package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.extension.debugFailUnreachable
import app.atomofiron.searchboxapp.utils.colorAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemSearchTaskBinding
import app.atomofiron.searchboxapp.custom.drawable.MuonsDrawable.Speed
import app.atomofiron.searchboxapp.model.finder.QueryParams
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.finder.SearchStatus
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.utils.Alpha

class SearchTaskHolder<Result : SearchResult>(
    parent: ViewGroup,
    listener: OnActionListener<Result>,
) : CardViewHolder<FinderStateItem.Task<Result>>(parent, R.layout.item_search_task) {

    override val hungry = false

    private val binding = ItemSearchTaskBinding.bind(view)

    init {
        itemView.setOnClickListener {
            listener.onItemClick(item)
        }
        binding.action.setOnClickListener { view ->
            view.isEnabled = false
            val item = item
            when (item.task.status) {
                is SearchStatus.Progress -> listener.onTaskStopClick(item)
                is SearchStatus.Ended -> listener.onTaskRemoveClick(item)
                is SearchStatus.Stopping -> debugFailUnreachable()
            }
        }
        binding.params.setSingleLine()
    }

    override fun minWidth(): Float = itemView.resources.getDimension(R.dimen.finder_task)

    override fun onBind(item: FinderStateItem.Task<Result>, position: Int) = binding.run {
        val task = item.task
        params.setParams(task.query)
        status.setStatus(task.result)
        action.isActivated = !task.isProgress
        progress.isInvisible = !task.status.running
        progress.setSpeed(if (task.isProgress) Speed.Medium else Speed.Slow)
        uncached.isVisible = item.task.isEnded && !item.task.cached

        val idLabel = if (task.isError) R.string.error else when (task.status) {
            is SearchStatus.Progress -> R.string.started
            is SearchStatus.Stopping -> R.string.stopping
            is SearchStatus.Ended -> if (task.status.stopped) R.string.stopped else R.string.completed
        }
        val colorLabel = when {
            task.isError -> context.colorAttr(MaterialAttr.colorError)
            else -> context.colorAttr(MaterialAttr.colorAccent)
        }
        label.setText(idLabel)
        label.setTextColor(colorLabel)

        val idAction = when {
            task.status.running -> R.string.stop
            else -> R.string.remove
        }
        action.setText(idAction)
        action.isEnabled = task.isProgress || task.isRemovable
        itemView.isEnabled = item.clickableIfEmpty || !task.result.isEmpty
    }

    private fun TextView.setParams(params: QueryParams) {
        val status = SpannableStringBuilder("* * ").append(params.query)
        when {
            params.ignoreCase -> R.drawable.ic_params_case_off
            else -> R.drawable.ic_params_case_on
        }.let {
            status.setIcon(it, 0, 1)
        }
        when {
            params.regex -> R.drawable.ic_params_regex_on
            else -> R.drawable.ic_params_regex_off
        }.let {
            status.setIcon(it, 2, 3)
        }
        text = status
    }

    private fun TextView.setStatus(result: SearchResult) {
        val status = SpannableStringBuilder()
        val counters = result.getCounters()
        result.getCounters().forEachIndexed { index, it ->
            status.append("*$it  ")
            val resId = when {
                counters.size == 1 -> R.drawable.ic_status_file_match
                index == 0 -> R.drawable.ic_status_match
                index == 1 -> R.drawable.ic_status_file_match
                else -> R.drawable.ic_status_file_all
            }
            val star = status.lastIndexOf('*')
            status.setIcon(resId, star, star.inc())
        }
        text = status
    }

    private fun Spannable.setIcon(resId: Int, start: Int, end: Int, alpha: Int = Alpha.VISIBLE_INT) {
        val span = ImageSpan(context, resId, ImageSpan.ALIGN_BASELINE)
        span.drawable.alpha = alpha
        setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    interface OnActionListener<R : SearchResult> {
        fun onItemClick(item: FinderStateItem.Task<R>)
        fun onTaskStopClick(item: FinderStateItem.Task<R>)
        fun onTaskRemoveClick(item: FinderStateItem.Task<R>)
    }
}