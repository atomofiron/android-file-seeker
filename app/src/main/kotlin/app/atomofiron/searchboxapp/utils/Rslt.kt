package app.atomofiron.searchboxapp.utils

import app.atomofiron.common.util.forHumans

sealed class Rslt<T>(val isOk: Boolean) {

    abstract class Ok<T> : Rslt<T>(isOk = true) {
        companion object : Rslt.Ok<Unit>() {
            private data class Ok<T>(override val value: T) : Rslt.Ok<T>()
            override val value = Unit
            operator fun <T> invoke(value: T): Rslt.Ok<T> = Ok(value)
        }
        abstract val value: T
    }
    open class Err<T> : Rslt<T>(isOk = false) {
        companion object {
            private data class Err<T>(override val message: String) : Rslt.Err<T>()
            operator fun <T> invoke(message: String): Rslt.Err<T> = Err(message)
        }
        open val message: String get() = ""
    }
}

fun <T> T.toRslt() = Rslt.Ok(this)

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
