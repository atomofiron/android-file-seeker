package app.atomofiron.searchboxapp.utils

import app.atomofiron.common.util.forHumans

sealed class Rslt<T>(val isOk: Boolean) {

    abstract class Ok<T> : Rslt<T>(isOk = true) {
        companion object : Rslt.Ok<Unit>() {
            private data class Ok<T>(override val value: T) : Rslt.Ok<T>()
            override val value = Unit
            operator fun <T> invoke(value: T): Rslt.Ok<T> = Ok(value)
            override fun toString() = "Ok"
        }
        abstract val value: T
    }

    open class Err<T>(open val message: String = "") : Rslt<T>(isOk = false) {
        companion object : Rslt.Err<Unit>() {
            private data class Err<T>(override val message: String) : Rslt.Err<T>()
            operator fun <T> invoke(value: T): Rslt.Err<T> = Err(value)
            override fun toString() = "Err"
        }
        val isEmpty: Boolean get() = message.isEmpty()
    }

    fun ok(): Ok<T>? = this as? Ok<T>

    fun err(): Err<T>? = this as? Err<T>
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

inline fun <T> Rslt<T>.ifOk(action: (T) -> Unit): Rslt<T> {
    ok()?.let { action(it.value) }
    return this
}
