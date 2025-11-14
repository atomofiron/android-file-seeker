package app.atomofiron.common.util

import app.atomofiron.searchboxapp.model.other.UniText

sealed interface AlertMessage {
    companion object {
        operator fun invoke(message: String, important: Boolean = false) = Uni(message, important)
        operator fun invoke(message: Int, important: Boolean = false) = Uni(message, important)
        operator fun <T> invoke(message: T, important: Boolean = false) = Other(message, important)
    }
    data class Uni(val message: UniText, override val important: Boolean = false) : AlertMessage {
        constructor(message: String, important: Boolean = false) : this(UniText(message), important)
        constructor(message: Int, important: Boolean = false) : this(UniText(message), important)
    }
    data class Other<T>(val message: T, override val important: Boolean = false) : AlertMessage

    val important: Boolean
}