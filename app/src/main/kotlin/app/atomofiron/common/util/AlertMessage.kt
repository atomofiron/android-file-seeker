package app.atomofiron.common.util

sealed interface AlertMessage {
    companion object {
        operator fun invoke(message: String, important: Boolean = false) = Str(message, important)
        operator fun invoke(message: Int, important: Boolean = false) = Res(message, important)
        operator fun <T> invoke(message: T, important: Boolean = false) = Other(message, important)
    }
    data class Str(val message: String, override val important: Boolean = false) : AlertMessage
    data class Res(val message: Int, override val important: Boolean = false) : AlertMessage
    data class Other<T>(val message: T, override val important: Boolean = false) : AlertMessage

    val important: Boolean
}