package app.atomofiron.common.util

import androidx.annotation.StringRes
import app.atomofiron.searchboxapp.model.other.UniText

sealed interface Alert {
    companion object {

        operator fun invoke(
            message: UniText,
            error: Boolean = false,
            important: Boolean = false,
        ) = Uni(message, error, important)

        operator fun invoke(
            @StringRes stringId: Int,
            error: Boolean = false,
            important: Boolean = false,
        ) = Uni(UniText(stringId), error, important)

        operator fun invoke(
            message: String,
            error: Boolean = false,
            important: Boolean = false,
        ) = Uni(UniText(message), error, important)
    }

    val important: Boolean
    val error: Boolean

    data class Uni(
        val text: UniText,
        override val error: Boolean = false,
        override val important: Boolean = false,
    ) : Alert

    abstract class Other(
        override val error: Boolean = false,
        override val important: Boolean = false,
    ) : Alert
}

@Suppress("FunctionName")
fun AlertErr(message: String, important: Boolean = false) = Alert(message, error = true, important)

@Suppress("FunctionName")
fun AlertErr(@StringRes message: Int, important: Boolean = false) = Alert(message, error = true, important)
