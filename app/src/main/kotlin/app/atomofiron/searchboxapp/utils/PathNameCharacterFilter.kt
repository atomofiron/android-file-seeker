package app.atomofiron.searchboxapp.utils

import android.text.InputFilter
import android.text.Spanned

class PathNameCharacterFilter : InputFilter {

    private val regex = Regex("[${Regex.escape(Const.PATH_INVALID_CHARS)}]")

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int,
    ): CharSequence {
        source ?: return ""
        return filter(source.substring(start, end))
    }

    fun filter(source: CharSequence): CharSequence = when {
        source.isEmpty() -> ""
        source.length > 1 -> source.replace(regex, Const.CHAR_REPLACEMENT)
        source.first() in Const.PATH_INVALID_CHARS -> ""
        else -> source
    }
}