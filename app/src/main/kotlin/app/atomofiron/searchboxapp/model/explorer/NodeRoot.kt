package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.utils.ExplorerUtils.asRoot
import java.util.Objects


data class NodeRoot(
    val type: NodeRootType,
    val item: Node,
    val sorting: NodeSorting,
    val thumbnail: Thumbnail.FilePath? = null,
    val thumbnailPath: String = "",
    // todo make true due rendering only
    val isSelected: Boolean = false,
    val pathVariants: Array<out String>? = null,
) {

    constructor(type: NodeRootType, sorting: NodeSorting, vararg pathVariants: String)
            : this(type, Node.asRoot(pathVariants.first(), type), sorting, pathVariants = pathVariants.takeIf { it.size > 1 })

    val stableId: Int = type.stableId
    val withPreview: Boolean = when (type) {
        is NodeRootType.Photos,
        is NodeRootType.Videos,
        is NodeRootType.Camera,
        is NodeRootType.Screenshots -> true
        else -> false
    }

    sealed class NodeRootType(val editable: Boolean = false) {
        val stableId: Int = Objects.hash(this::class)

        data object Photos : NodeRootType()
        data object Videos : NodeRootType()
        data object Camera : NodeRootType()
        data object Screenshots : NodeRootType()
        data object Downloads : NodeRootType(editable = true)
        data object Bluetooth : NodeRootType(editable = true)
        data class InternalStorage(
            val used: Long = 0,
            val free: Long = 0,
        ) : NodeRootType(editable = true)
        data object Favorite : NodeRootType()
    }

    override fun equals(other: Any?): Boolean = when {
        other !is NodeRoot -> false
        other.type != type -> false
        other.thumbnail != thumbnail -> false
        other.isSelected != isSelected -> false
        !other.item.areContentsTheSame(item) -> false
        else -> true
    }

    override fun hashCode(): Int = Objects.hash(type, thumbnail)
}
