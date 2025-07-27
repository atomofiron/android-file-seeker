package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isGone
import androidx.core.view.isVisible
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.RegexInputField
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.Query
import app.atomofiron.searchboxapp.utils.Alpha
import java.util.regex.Pattern

class QueryFieldHolder(
    parent: ViewGroup,
    private val listener: OnActionListener,
) : GeneralHolder<FinderStateItem>(parent, R.layout.item_query_field) {

    override val hungry = true

    private val params: Query get() = item as Query

    private val etFind = itemView.findViewById<RegexInputField>(R.id.item_find_rt_find)
    private val btnFind = itemView.findViewById<View>(R.id.item_find_ib_find)
    private val viewReplace = itemView.findViewById<View>(R.id.item_find_i_replace)

    init {
        btnFind.setOnClickListener {
            listener.onSearchClick(etFind.text.toString())
        }
        etFind.addTextChangedListener(this@QueryFieldHolder.TextChangeListener())
        etFind.setOnEditorActionListener(::onEditorAction)
    }

    override fun minWidth(): Float = itemView.resources.getDimension(R.dimen.finder_query_field)

    override fun onBind(item: FinderStateItem, position: Int) {
        item as Query
        viewReplace.isVisible = item.replaceEnabled
        etFind.imeOptions = when {
            item.replaceEnabled -> (etFind.imeOptions and EditorInfo.IME_ACTION_SEARCH.inv()) or EditorInfo.IME_ACTION_NEXT
            else -> (etFind.imeOptions and EditorInfo.IME_ACTION_NEXT.inv()) or EditorInfo.IME_ACTION_SEARCH
        }
        if (etFind.text.toString() != item.query) {
            etFind.setText(item.query)
            etFind.setSelection(item.query.length)
        }
        updateWarning(etFind.text.toString())
        btnFind.isEnabled = item.available
        btnFind.alpha = Alpha.enabled(item.available)
    }

    private fun onEditorAction(view: View, actionId: Int, /* indeed nullable */ event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            val query = etFind.text.toString()
            when {
                query.isEmpty() -> return true
                else -> listener.onSearchClick(query)
            }
        }
        return false
    }

    private fun updateWarning(query: String) {
        if (params.useRegex) {
            try {
                Pattern.compile(query)
            } catch (e: Exception) {
                etFind.isActivated = true
                btnFind.isGone = true
                return
            }
        }
        etFind.isActivated = false
    }

    interface OnActionListener {
        fun onSearchClick(value: String)
        fun onSearchChange(value: String)
        fun onReplaceClick(value: String)
    }

    inner class TextChangeListener : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: Editable) {
            val value = s.toString()
            updateWarning(value)

            val item = item as Query
            if (value != item.query) {
                listener.onSearchChange(value)
            }
        }
    }
}