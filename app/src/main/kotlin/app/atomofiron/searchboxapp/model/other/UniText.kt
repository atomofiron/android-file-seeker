package app.atomofiron.searchboxapp.model.other

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import app.atomofiron.searchboxapp.model.other.UniText.Fmt
import app.atomofiron.searchboxapp.model.other.UniText.Plr
import app.atomofiron.searchboxapp.model.other.UniText.Res
import app.atomofiron.searchboxapp.model.other.UniText.Str

sealed interface UniText {
    @JvmInline
    value class Res(@StringRes val value: Int) : UniText
    @JvmInline
    value class Str(val value: String) : UniText

    data class Fmt(@StringRes val res: Int, val args: List<Any>) : UniText

    data class Plr(@PluralsRes val res: Int, val quantity: Int, val args: List<Any>) : UniText

    companion object {
        operator fun invoke(text: String) = Str(text)
        operator fun invoke(text: String?) = text?.let { Str(text) }
        operator fun invoke(@StringRes res: Int) = Res(res)
        operator fun invoke(@StringRes res: Int, vararg args: Any) = Fmt(res, args.toList())
        operator fun invoke(@StringRes res: Int, args: List<Any>) = Fmt(res, args)
        operator fun invoke(@PluralsRes res: Int, quantity: Int, vararg args: Any) = Plr(res, quantity, args.toList())
    }
}

@JvmName("getNullable")
operator fun Resources.get(text: UniText?) = text?.let { get(it) }

operator fun Resources.get(text: UniText) = when (text) {
    is Str -> text.value
    is Res -> getString(text.value)
    is Fmt -> getString(text.res, *text.args.toTypedArray())
    is Plr -> getQuantityString(text.res, text.quantity, *text.args.toTypedArray())
}

fun String.toUni() = UniText(this)
