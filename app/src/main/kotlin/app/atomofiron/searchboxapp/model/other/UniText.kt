package app.atomofiron.searchboxapp.model.other

import android.content.res.Resources
import androidx.annotation.StringRes
import app.atomofiron.searchboxapp.model.other.UniText.Fmt
import app.atomofiron.searchboxapp.model.other.UniText.Res
import app.atomofiron.searchboxapp.model.other.UniText.Str

sealed interface UniText {
    @JvmInline
    value class Res(@StringRes val value: Int) : UniText
    @JvmInline
    value class Str(val value: String) : UniText

    data class Fmt(@StringRes val res: Int, val args: List<Any>) : UniText

    companion object {
        operator fun invoke(text: String) = Str(text)
        operator fun invoke(text: String?) = text?.let { Str(text) }
        operator fun invoke(@StringRes text: Int) = Res(text)
        operator fun invoke(@StringRes text: Int, vararg args: Any) = Fmt(text, args.toList())
    }
}

@JvmName("getNullable")
operator fun Resources.get(text: UniText?) = text?.let { get(it) }

operator fun Resources.get(text: UniText) = when (text) {
    is Fmt -> getString(text.res, text.args)
    is Res -> getString(text.value)
    is Str -> text.value
}

fun String.toUni() = UniText(this)
