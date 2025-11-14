package app.atomofiron.searchboxapp.utils

import android.text.InputFilter
import android.text.Spanned

class PathNameCharacterFilter : InputFilter {

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int,
    ): CharSequence {
        source ?: return ""
        val insert = source.substring(start, end)
        return when {
            insert.isEmpty() -> ""
            insert.length > 1 -> insert.replace(Const.PATH_INVALID_CHAR, Const.CHAR_REPLACEMENT)
            insert.first() == Const.PATH_INVALID_CHAR -> ""
            else -> insert
        }
    }
}