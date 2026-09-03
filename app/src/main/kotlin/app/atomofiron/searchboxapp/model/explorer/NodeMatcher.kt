package app.atomofiron.searchboxapp.model.explorer

class NodeMatcher(
    private val treeSize: Int,
    private val mimeTypes: List<String>,
    private val onlyPhotos: Boolean,
    private val onlyVideos: Boolean,
    private val onlyMedia: Boolean,
) {
    constructor(
        treeSize: Int,
        mimeTypes: List<String>,
        info: NodeRootInfo,
        option: NodeRootOption.CameraToggle?,
    ) : this(
        treeSize,
        mimeTypes,
        onlyPhotos = info is NodeRootInfo.Screenshots || info is NodeRootInfo.Camera && option?.photos() == true,
        onlyVideos = info is NodeRootInfo.Camera && option?.videos() == true,
        onlyMedia = info is NodeRootInfo.Camera,
    )

    val filteredCounters: IntArray? = when {
        onlyPhotos || onlyVideos || onlyMedia -> Unit
        mimeTypes.isEmpty() -> null
        mimeTypes == NodeContent.Directory.mimeTypes -> null
        else -> Unit
    }?.let { IntArray(treeSize) }

    fun matches(item: Node, levelIndex: Int): Boolean = when {
        item.isDirectory -> true
        mimeTypes.isNotEmpty() && item.isFile && !item.content.matchesAny(mimeTypes) -> false
        onlyPhotos && !item.content.isPicture() -> false
        onlyVideos && !item.content.isMovie() -> false
        onlyMedia && !item.content.isMedia() -> false
        else -> true
    }.also { if (!it) filteredCounters?.inc(levelIndex) }

    private fun IntArray.inc(index: Int) = set(index, get(index).inc())
}
