package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isInvisible
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.common.util.Unreachable
import app.atomofiron.searchboxapp.custom.view.MuonsView
import app.atomofiron.searchboxapp.model.finder.SearchParams
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.model.finder.SearchState
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.utils.Alpha

class TaskHolder(parent: ViewGroup, listener: OnActionListener) : CardViewHolder(parent, R.layout.item_progress) {

    override val hungry = false

    private val tvLabel = itemView.findViewById<TextView>(R.id.progress_tv_label)
    private val tvParams = itemView.findViewById<TextView>(R.id.progress_tv_params)
    private val tvStatus = itemView.findViewById<TextView>(R.id.progress_tv_status)
    private val bView = itemView.findViewById<MuonsView>(R.id.item_explorer_ps)
    private val btnAction = itemView.findViewById<Button>(R.id.progress_btn_action)

    init {
        itemView.setOnClickListener {
            listener.onItemClick(item as FinderStateItem.Task)
        }
        btnAction.setOnClickListener { view ->
            view.isEnabled = false
            val item = item as FinderStateItem.Task
            when (item.task.state) {
                is SearchState.Progress -> listener.onProgressStopClick(item)
                is SearchState.Stopping -> Unreachable
                is SearchState.Stopped,
                is SearchState.Ended -> listener.onProgressRemoveClick(item)
            }
        }
        tvParams.setSingleLine()
    }

    override fun minWidth(): Float = itemView.resources.getDimension(R.dimen.finder_task)

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.Task
        val task = item.task
        tvParams.setParams(task.params)
        tvStatus.setStatus(task.result)
        btnAction.isActivated = !task.inProgress
        bView.isInvisible = !task.state.running

        val idLabel = if (task.isError) R.string.error else when (task.state) {
            is SearchState.Progress -> R.string.started
            is SearchState.Stopping -> R.string.stopping
            is SearchState.Stopped -> R.string.stopped
            is SearchState.Ended -> R.string.done
        }
        val colorLabel = when {
            task.isError -> context.findColorByAttr(MaterialAttr.colorError)
            else -> context.findColorByAttr(MaterialAttr.colorAccent)
        }
        tvLabel.setText(idLabel)
        tvLabel.setTextColor(colorLabel)

        val idAction = when {
            task.state.running -> R.string.stop
            else -> R.string.remove
        }
        btnAction.setText(idAction)
        btnAction.isEnabled = task.inProgress || task.state.removable
    }

    private fun TextView.setParams(params: SearchParams) {
        val status = SpannableStringBuilder("* * ").append(params.query)
        when {
            params.ignoreCase -> R.drawable.ic_params_case_off
            else -> R.drawable.ic_params_case_on
        }.let {
            status.setIcon(it, 0, 1)
        }
        when {
            params.useRegex -> R.drawable.ic_params_regex_on
            else -> R.drawable.ic_params_regex_off
        }.let {
            status.setIcon(it, 2, 3)
        }
        text = status
    }

    private fun TextView.setStatus(result: SearchResult) {
        val status = SpannableStringBuilder()
        result.getCounters().forEachIndexed { index, it ->
            status.append("*$it  ")
            val resId = when (index) {
                0 -> R.drawable.ic_status_match
                1 -> R.drawable.ic_status_file_match
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

    interface OnActionListener {
        fun onItemClick(item: FinderStateItem.Task)
        fun onProgressStopClick(item: FinderStateItem.Task)
        fun onProgressRemoveClick(item: FinderStateItem.Task)
    }
}