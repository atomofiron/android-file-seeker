package app.atomofiron.searchboxapp.model.explorer

sealed class NodeRootInfo(
    val removable: Boolean = false,
) {
    data object SystemRoot : NodeRootInfo()
    data object Camera : NodeRootInfo()
    data object Screenshots : NodeRootInfo()
    data object Downloads : NodeRootInfo()
    data object Bluetooth : NodeRootInfo()
    data class Storage(val info: NodeStorage) : NodeRootInfo(info.kind.removable) {
        val kind = info.kind
        val total = info.total
        val used = info.used
    }
    data object Favorite : NodeRootInfo()
}
