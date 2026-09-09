package app.atomofiron.searchboxapp.model.explorer

sealed class NodeRootInfo(
    val removable: Boolean = false,
) {
    open val id get() = this::class.hashCode()

    data object SystemRoot : NodeRootInfo()
    data object Camera : NodeRootInfo()
    data object Screenshots : NodeRootInfo()
    data object Downloads : NodeRootInfo()
    data object Bluetooth : NodeRootInfo()
    data class Storage(val info: NodeStorage) : NodeRootInfo(info.kind.removable) {

        override val id get() = info.path.hashCode()

        val kind = info.kind
        val total = info.total
        val used = info.used

        override fun theSame(other: NodeRootInfo) = other is Storage && other.info.path == info.path
    }
    data object Favorite : NodeRootInfo()

    open fun theSame(other: NodeRootInfo) = this == other
}
