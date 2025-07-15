package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.text.InputFilter
import android.text.Spanned
import android.view.ViewGroup
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemTextFieldBinding
import app.atomofiron.searchboxapp.custom.view.TextField
import app.atomofiron.searchboxapp.screens.finder.state.FinderStateItem

private const val SEPARATOR = " "
private val SEPARATORS = Regex("$SEPARATOR+")

class EditCharactersHolder(
    parent: ViewGroup,
    private val output: OnEditCharactersListener,
) : GeneralHolder<FinderStateItem>(parent, R.layout.item_text_field), InputFilter, TextField.OnSubmitListener {

    override val hungry = true

    private val binding = ItemTextFieldBinding.bind(itemView)
    private val delegate = TextFieldHolderDelegate(binding)

    init {
        binding.box.setHint(R.string.pref_special_characters)
        binding.field.run {
            maxLines = 1
            addOnSubmitListener(this@EditCharactersHolder)
            filters += arrayOf<InputFilter>(this@EditCharactersHolder)
        }
    }

    override fun minWidth(): Float = delegate.minWidth()

    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence {
        source ?: return ""
        val destination = dest ?: ""
        val space = SEPARATOR.first()
        val spaceOnLeft = destination.getOrNull(dstart.dec()) == space
        val spaceOnRight = destination.getOrNull(dend) == space
        var corrected = source.replace(SEPARATORS, SEPARATOR)
        if (spaceOnLeft && corrected.firstOrNull() == space) {
            corrected = corrected.substring(1, corrected.length)
        }
        if (spaceOnRight && corrected.lastOrNull() == space) {
            corrected = corrected.substring(0, corrected.length.dec())
        }
        return corrected
    }

    override fun onBind(item: FinderStateItem, position: Int) {
        item as FinderStateItem.EditCharacters
        item.value
            .filter { it.isNotBlank() }
            .joinToString(separator = SEPARATOR)
            .let { binding.field.setText(it) }
    }

    override fun onSubmit(value: String) = output.onEditCharacters(value.split(SEPARATOR))

    interface OnEditCharactersListener {
        fun onEditCharacters(new: List<String>)
    }
}