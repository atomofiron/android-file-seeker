package app.atomofiron.searchboxapp.model.network

import kotlinx.serialization.Serializable

@Serializable
class GithubError(
    val status: String,
    val message: String,
)
