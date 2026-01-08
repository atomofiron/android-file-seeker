package app.atomofiron.searchboxapp.model.explorer

sealed class NodeRootInfo(
    val removable: Boolean = false,
    val temp: Int = 0,
) {
    data object SystemRoot : NodeRootInfo()
    // todo replace with Camera with segmented buttons and remove NodeRootType.temp
    data object Photos : NodeRootInfo()
    data object Videos : NodeRootInfo(temp = 1)
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
