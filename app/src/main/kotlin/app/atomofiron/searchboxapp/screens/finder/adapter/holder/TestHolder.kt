package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.text.Editable
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemTextFieldBinding
import app.atomofiron.searchboxapp.custom.drawable.makeHoled
import app.atomofiron.searchboxapp.custom.view.style.RoundedBackgroundSpan
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem.TestField
import java.util.regex.Pattern

class TestHolder(
    parent: ViewGroup,
    private val output: OnTestChangeListener,
) : GeneralHolder<FinderStateItem>(parent, R.layout.item_text_field), TextWatcher {

    override val hungry = true

    private val binding = ItemTextFieldBinding.bind(itemView)
    override val itemOrNull get() = super.itemOrNull as TestField?
    private val default = parent.resources.getString(R.string.pangram)
    private val span get() = RoundedBackgroundSpan(
        backgroundColor = context.findColorByAttr(MaterialAttr.colorSurfaceVariant),
        borderColor = context.findColorByAttr(MaterialAttr.colorSecondary),
        textColor = context.findColorByAttr(MaterialAttr.colorOnSurfaceVariant),
        context.resources.getDimension(R.dimen.background_span_corner_radius),
        context.resources.getDimension(R.dimen.background_span_border_thickness),
    )

    init {
        binding.box.setHint(R.string.try_find_this_text)
        binding.field.run {
            isSingleLine = false
            maxLines = 5
            setText(default)
            addTextChangedListener(this@TestHolder)
            imeOptions = imeOptions and EditorInfo.IME_ACTION_DONE.inv()
            inputType = inputType or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }
        binding.field.makeHoled(binding.box)
    }

    override fun minWidth(): Float = itemView.resources.getDimension(R.dimen.finder_test_field)

    override fun onBind(item: FinderStateItem, position: Int) {
        item as TestField
        val new = item.value ?: default
        if (new != binding.field.text?.toString()) {
            binding.field.setText(new)
        }
    }

    private fun test(item: TestField) = binding.run {
        val text = SpannableStringBuilder(item.value)
        text.getSpans(0, text.length, RoundedBackgroundSpan::class.java).forEach {
            text.removeSpan(it)
        }
        when {
            item.query.isEmpty() -> Unit
            item.useRegex -> testSearchWithRegexp(item)
            else -> testSearch(item)
        }
    }

    private fun testSearch(item: TestField) = binding.run {
        val text = field.text ?: return
        var offset = 0
        val length = item.query.length
        var index = text.indexOf(item.query, offset, item.ignoreCase)
        while (index != -1) {
            text.setSpan(span, index, index + length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            offset = index + length
            index = text.indexOf(item.query, offset, item.ignoreCase)
        }
    }

    private fun testSearchWithRegexp(item: TestField) {
        val text = binding.field.text ?: return
        var flags = 0
        if (item.ignoreCase) {
            flags = flags or Pattern.CASE_INSENSITIVE
        }
        val pattern: Pattern
        try {
            pattern = Pattern.compile(item.query, flags)

            var offset = 0
            val lines = text.toString().split('\n')
            for (line in lines) {
                val matcher = pattern.matcher(line)

                while (matcher.find() && matcher.start() != matcher.end()) {
                    text.setSpan(span, offset + matcher.start(), offset + matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                offset += line.length.inc()
            }
        } catch (e: Exception) {
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

    override fun afterTextChanged(s: Editable?) {
        val item = itemOrNull ?: return
        val string = s?.toString()
        when (string) {
            null, item.value -> Unit
            default -> output.onTestTextChange(null)
            else -> output.onTestTextChange(string)
        }
        test(item.copy(value = string ?: ""))
    }

    interface OnTestChangeListener {
        fun onTestTextChange(value: String?)
    }
}