package app.atomofiron.searchboxapp.model.explorer

sealed interface NodeRootOption {

    val id: Int
    fun similar(other: NodeRootOption): Boolean

    enum class CameraToggle : NodeRootOption {
        Photos,
        All,
        Videos,
        ;
        override val id = 0

        fun photos() = this == Photos
        fun all() = this == All
        fun videos() = this == Videos

        override fun similar(other: NodeRootOption) = other is CameraToggle
    }
}
