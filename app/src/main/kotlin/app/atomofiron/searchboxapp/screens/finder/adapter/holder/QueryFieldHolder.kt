package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
import androidx.core.view.isVisible
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.common.util.hideKeyboard
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemQueryFieldBinding
import app.atomofiron.searchboxapp.custom.drawable.makeHoled
import app.atomofiron.searchboxapp.custom.view.TextField
import app.atomofiron.searchboxapp.custom.view.makeRegex
import app.atomofiron.searchboxapp.custom.view.showError
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.Query
import app.atomofiron.searchboxapp.utils.updateLayoutParams
import java.util.regex.Pattern

class QueryFieldHolder(
    parent: ViewGroup,
    private val listener: OnActionListener,
) : GeneralHolder<FinderStateItem>(parent, R.layout.item_query_field)
    , TextField.Listener
    , View.OnFocusChangeListener
    , View.OnLayoutChangeListener
{
    override val hungry = true
    override val item get() = super.item as Query

    private val binding = ItemQueryFieldBinding.bind(itemView)
    private val textLayout = binding.queryField.box
    val textField = binding.queryField.field

    init {
        binding.button.setOnClickListener {
            listener.onSearchClick(textField.text.toString())
            textField.hideKeyboard()
        }
        textField.run {
            imeOptions = (imeOptions and IME_ACTION_DONE.inv()) or IME_ACTION_SEARCH
            textLayout.hint = resources.getString(R.string.searching)
            makeRegex()
            makeHoled(binding.queryField.box)
            addTextChangedListener(this@QueryFieldHolder.TextChangeListener())
            addListener(this@QueryFieldHolder)
            addOnFocusChangeListener(this@QueryFieldHolder)
        }
        binding.clear.setOnClickListener { textField.text?.clear() }
        binding.root.addOnLayoutChangeListener(this)
    }

    override fun onLayoutChange(v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
        if (textLayout.error == null) {
            binding.button.updateLayoutParams(width = textLayout.height, height = textLayout.height)
        }
    }

    override fun minWidth(): Float = itemView.resources.getDimension(R.dimen.finder_query_field)

    override fun onBind(item: FinderStateItem, position: Int) {
        item as Query
        if (textField.text.toString() != item.query) {
            textField.setText(item.query)
            textField.setSelection(item.query.length)
        }
        bindState(item.query)
    }

    override fun onAction(value: String, code: Int) {
        if (code == IME_ACTION_SEARCH && value.isNotEmpty()) {
            listener.onSearchClick(value)
        }
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        binding.clear.isActivated = hasFocus
    }

    private fun bindState(query: String) {
        try {
            if (item.useRegex) Pattern.compile(query)
            textLayout.showError(false)
        } catch (e: Exception) {
            textLayout.showError(e.message?.lineSequence()?.first())
        }
        val error = textLayout.error != null
        val enabled = !error && item.enabled
        binding.button.isEnabled = enabled
        binding.clear.isVisible = textField.length() > 0
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
            if (value != item.query) {
                listener.onSearchChange(value)
            }
        }
    }
}