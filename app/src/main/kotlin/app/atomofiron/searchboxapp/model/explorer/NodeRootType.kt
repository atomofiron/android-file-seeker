package app.atomofiron.searchboxapp.model.explorer

import java.util.Objects

sealed class NodeRootType(
    open val editable: Boolean = false,
    val removable: Boolean = false,
    val withPreview: Boolean = false,
) {
    open val stableId: Int = Objects.hash(this::class)

    data object Photos : NodeRootType(withPreview = true)
    data object Videos : NodeRootType(withPreview = true)
    data object Camera : NodeRootType(withPreview = true)
    data object Screenshots : NodeRootType(withPreview = true)
    data object Downloads : NodeRootType(editable = true)
    data object Bluetooth : NodeRootType(editable = true)
    data class Storage(
        val info: NodeStorage,
        override val editable: Boolean = true,
    ) : NodeRootType(editable, info.kind.removable) {
        override val stableId: Int = info.path.hashCode()
        val kind = info.kind
        val total = info.total
        val used = info.used
    }
    data object Favorite : NodeRootType()
}
