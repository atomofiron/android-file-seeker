package app.atomofiron.searchboxapp.model.network

import kotlinx.serialization.Serializable

@Serializable
data class GithubRelease(
    val publishedAt: String,
    val name: String,
    val body: String, // description
    val assets: List<GithubAsset>,
) {
    fun isNewerThan(threshold: String) = publishedAt > threshold
}
