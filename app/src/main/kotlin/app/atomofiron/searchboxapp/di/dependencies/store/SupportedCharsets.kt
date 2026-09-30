package app.atomofiron.searchboxapp.di.dependencies.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import uniffi.native_lib.SupportedCharset

val DefaultCharset = Charsets.UTF_8
val DefaultCharsetName = DefaultCharset.name()

class SupportedCharsets {

    val list: StateFlow<List<SupportedCharset>>
        field = MutableStateFlow(emptyList())

    fun set(list: List<SupportedCharset>) {
        this.list.value = list
    }
}