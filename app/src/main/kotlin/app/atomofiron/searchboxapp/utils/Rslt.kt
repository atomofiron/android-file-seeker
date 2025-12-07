package app.atomofiron.searchboxapp.utils

import app.atomofiron.common.util.forHumans

sealed class Rslt<T>(val isOk: Boolean) {
    open val value: T? = null

    abstract class Ok<T> : Rslt<T>(isOk = true) {
        companion object : Rslt.Ok<Unit>() {
            private data class Ok<T>(override val value: T) : Rslt.Ok<T>()
            override val value = Unit
            operator fun <T> invoke(value: T): Rslt.Ok<T> = Ok(value)
        }
        abstract override val value: T
    }
    open class Err<T>(open val message: String = "") : Rslt<T>(isOk = false) {
        val isEmpty: Boolean get() = message.isEmpty()
    }
}

fun <T> T.toOk() = Rslt.Ok(this)

fun <T> String.toErr() = Rslt.Err<T>(this)

fun <T, E : Throwable> E.toRslt() = Rslt.Err<T>(forHumans())

fun <T> Rslt<T>.unwrapOrNull(): T? = when (this) {
    is Rslt.Ok -> value
    is Rslt.Err -> null
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T> Rslt<T>.unwrapOr(value: T): T = when (this) {
    is Rslt.Ok -> this@unwrapOr.value
    is Rslt.Err -> value
}

inline fun <T> Rslt<T>.unwrapOrElse(action: (message: String) -> T): T = when (this) {
    is Rslt.Ok -> value
    is Rslt.Err -> action(message)
}

inline fun <T, R> Rslt<T>.map(map: (T) -> R): Rslt<R> = when (this) {
    is Rslt.Ok -> Rslt.Ok(map(value))
    is Rslt.Err -> Rslt.Err(message)
}

inline fun <T> Rslt<T>.ifErr(action: (String) -> Unit): Rslt<T> {
    if (this is Rslt.Err) {
        action(message)
    }
    return this
}
