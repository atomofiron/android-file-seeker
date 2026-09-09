package app.atomofiron.searchboxapp.model.explorer

sealed interface NodeRootOption {

    val id: Int
    val onlyPhotos: Boolean get() = false
    val onlyVideos: Boolean get() = false
    val onlyMedia: Boolean get() = false
    fun similar(other: NodeRootOption): Boolean

    enum class CameraToggle(
        override val onlyPhotos: Boolean = false,
        override val onlyVideos: Boolean = false,
        override val onlyMedia: Boolean = false,
    ) : NodeRootOption {
        Photos(onlyPhotos = true),
        All(onlyMedia = true),
        Videos(onlyVideos = true),
        ;
        override val id = 0

        fun photos() = this == Photos
        fun all() = this == All
        fun videos() = this == Videos

        override fun similar(other: NodeRootOption) = other is CameraToggle
    }
}
