package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.utils.ExplorerUtils.asRoot
import java.util.Objects

data class NodeRoot(
    val type: NodeRootType,
    val item: Node,
    val sorting: NodeSorting,
    val thumbnail: Thumbnail? = null,
    val thumbnailPath: String = "",
    // isSelected is always false in the garden
    val isSelected: Boolean = false,
    val pathVariants: Array<out String>? = null,
) {

    constructor(type: NodeRootType, sorting: NodeSorting, vararg pathVariants: String)
            : this(type, Node.asRoot(pathVariants.first(), type), sorting, pathVariants = pathVariants.takeIf { it.size > 1 })

    val stableId: Int = type.stableId
    val isEnabled: Boolean get() = item.isCached || type is NodeRootType.Storage
    val withPreview: Boolean = type.withPreview

    init {
        require(item.children?.isOpened != true)
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
