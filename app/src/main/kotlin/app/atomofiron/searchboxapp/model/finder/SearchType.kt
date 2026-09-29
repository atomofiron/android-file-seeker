package app.atomofiron.searchboxapp.model.finder

import kotlinx.serialization.Serializable

@Serializable
sealed interface SearchType {

    val charset: String? get() = null

    @Serializable
    data object Names : SearchType

    @Serializable
    data class Text(override val charset: String?) : SearchType
}