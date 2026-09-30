package app.atomofiron.searchboxapp.model.finder

import app.atomofiron.searchboxapp.di.dependencies.store.DefaultCharsetName
import kotlinx.serialization.Serializable

@Serializable
sealed interface SearchType {

    val charset: String get() = DefaultCharsetName

    @Serializable
    data object Names : SearchType

    @Serializable
    data class Text(override val charset: String) : SearchType
}