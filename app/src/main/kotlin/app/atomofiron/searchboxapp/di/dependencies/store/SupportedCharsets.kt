package app.atomofiron.searchboxapp.di.dependencies.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import uniffi.native_lib.SupportedCharset
import javax.inject.Inject
import javax.inject.Singleton

val DefaultCharset = Charsets.UTF_8
val DefaultCharsetName = DefaultCharset.name()

@Singleton
class SupportedCharsets @Inject constructor() {

    val list: StateFlow<List<SupportedCharset>>
        field = MutableStateFlow(emptyList())

    fun set(list: List<SupportedCharset>) {
        this.list.value = list
    }
}