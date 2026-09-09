package app.atomofiron.searchboxapp.model.explorer

sealed class NodeRootInfo(
    val removable: Boolean = false,
    val onlyPhotos: Boolean = false,
    val onlyVideos: Boolean = false,
    val onlyMedia: Boolean = false,
) {
    data object SystemRoot : NodeRootInfo()
    data object Camera : NodeRootInfo(onlyMedia = true)
    data object Screenshots : NodeRootInfo(onlyPhotos = true)
    data object Screencasts : NodeRootInfo(onlyVideos = true)
    data object Downloads : NodeRootInfo()
    data object Bluetooth : NodeRootInfo()
    data class Storage(val info: NodeStorage) : NodeRootInfo(info.kind.removable) {
        val kind = info.kind
        val total = info.total
        val used = info.used

        override fun theSame(other: NodeRootInfo) = other is Storage && other.info.path == info.path
    }
    data object Favorite : NodeRootInfo()

    open fun theSame(other: NodeRootInfo) = this == other
}
