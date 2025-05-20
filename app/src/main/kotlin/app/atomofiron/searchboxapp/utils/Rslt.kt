package app.atomofiron.searchboxapp.utils


sealed interface Rslt<T> {
    data class Err<T>(val error: String) : Rslt<T>
    data class Ok<T>(val data: T) : Rslt<T> {
        companion object {
            operator fun invoke() = Ok(Unit)
        }
    }
}

fun <T> T.toRslt() = Rslt.Ok(this)

inline fun <T> Rslt<T>.unwrapOrElse(action: (error: String) -> T): T = when (this) {
    is Rslt.Ok -> data
    is Rslt.Err -> action(error)
}
