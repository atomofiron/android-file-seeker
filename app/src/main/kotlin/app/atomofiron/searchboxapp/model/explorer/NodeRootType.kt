package app.atomofiron.searchboxapp.model.explorer

sealed class NodeRootType(
    val removable: Boolean = false,
    val withPreview: Boolean = false,
    val temp: Int = 0,
) {
    data object SystemRoot : NodeRootType()
    // todo replace with Camera with segmented buttons and remove NodeRootType.temp
    data object Photos : NodeRootType(withPreview = true)
    data object Videos : NodeRootType(withPreview = true, temp = 1)
    data object Camera : NodeRootType(withPreview = true)
    data object Screenshots : NodeRootType(withPreview = true)
    data object Downloads : NodeRootType()
    data object Bluetooth : NodeRootType()
    data class Storage(val info: NodeStorage) : NodeRootType(info.kind.removable) {
        val kind = info.kind
        val total = info.total
        val used = info.used
    }
    data object Favorite : NodeRootType()
}
