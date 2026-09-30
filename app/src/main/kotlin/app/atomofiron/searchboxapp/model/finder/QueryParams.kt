package app.atomofiron.searchboxapp.model.finder

import app.atomofiron.searchboxapp.di.dependencies.store.DefaultCharsetName
import kotlinx.serialization.Serializable

@Serializable
data class QueryParams(
    val query: String,
    val regex: Boolean,
    val ignoreCase: Boolean,
    val charset: String = DefaultCharsetName,
)