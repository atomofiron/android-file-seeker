package app.atomofiron.searchboxapp.model.other

import android.content.res.Resources
import androidx.annotation.StringRes
import app.atomofiron.searchboxapp.model.other.UniText.Res
import app.atomofiron.searchboxapp.model.other.UniText.Str

sealed interface UniText {
    @JvmInline
    value class Res(val value: Int) : UniText
    @JvmInline
    value class Str(val value: String) : UniText

    companion object {
        operator fun invoke(@StringRes text: Int) = Res(text)
        operator fun invoke(text: String) = Str(text)
    }

    operator fun get(resources: Resources) = when (this) {
        is Res -> resources.getString(value)
        is Str -> value
    }
}

fun String.uni() = UniText(this)

operator fun Resources.get(text: UniText?) = when (text) {
    null -> null
    is Res -> getString(text.value)
    is Str -> text.value
}
