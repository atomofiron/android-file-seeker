package app.atomofiron.searchboxapp.utils


sealed interface Rslt<T> {
    data class Ok<T>(val data: T) : Rslt<T>
    data class Err<T>(val error: String) : Rslt<T>
}
