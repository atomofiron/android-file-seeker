package app.atomofiron.searchboxapp.utils

sealed class Rslt<T> {
    class Ok<T>(val data: T) : Rslt<T>()
    class Err<T>(val error: String) : Rslt<T>()
}
