package app.atomofiron.searchboxapp.model

data class CacheConfig(
    val useSu: Boolean,
    // todo replace with Glide with custom loading for audio
    val thumbnailSize: Int = 1,
    val legacySizeBig: Boolean = false,
)