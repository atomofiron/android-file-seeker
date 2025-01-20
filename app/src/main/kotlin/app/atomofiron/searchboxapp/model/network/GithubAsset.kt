package app.atomofiron.searchboxapp.model.network

import kotlinx.serialization.Serializable

@Serializable
data class GithubAsset(
    val id: Int,
    val name: String,
    val size: Long,
    val browserDownloadUrl: String,
)
