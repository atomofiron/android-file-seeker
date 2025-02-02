package app.atomofiron.searchboxapp.model

data class CacheConfig(
    val useSu: Boolean,
    val thumbnailSize: Int = 1,
    val legacySizeBig: Boolean = false,
)