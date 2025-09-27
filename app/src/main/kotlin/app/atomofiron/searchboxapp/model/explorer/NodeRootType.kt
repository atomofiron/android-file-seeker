package app.atomofiron.searchboxapp.model.explorer

import java.util.Objects

sealed class NodeRootType(
    val removable: Boolean = false,
    val withPreview: Boolean = false,
    stableId: Int = 0,
) {
    val stableId: Int = if (stableId == 0) Objects.hash(this::class) else stableId

    data object SystemRoot : NodeRootType()
    data object Photos : NodeRootType(withPreview = true)
    data object Videos : NodeRootType(withPreview = true)
    data object Camera : NodeRootType(withPreview = true)
    data object Screenshots : NodeRootType(withPreview = true)
    data object Downloads : NodeRootType()
    data object Bluetooth : NodeRootType()
    data class Storage(val info: NodeStorage) : NodeRootType(info.kind.removable, stableId = info.path.hashCode()) {
        val kind = info.kind
        val total = info.total
        val used = info.used
    }
    data object Favorite : NodeRootType()
}
