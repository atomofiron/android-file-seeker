package app.atomofiron.searchboxapp.model.finder

import kotlinx.serialization.Serializable

@Serializable
data class QueryParams(
    val query: String,
    val regex: Boolean,
    val ignoreCase: Boolean,
)