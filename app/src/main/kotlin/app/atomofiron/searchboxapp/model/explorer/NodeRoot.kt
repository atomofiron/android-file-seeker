package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.extension.hash
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toRoot

data class NodeRoot(
    val info: NodeRootInfo,
    val item: Node,
    val previewSorting: NodeSorting? = null,
    val thumbnail: Thumbnail? = null,
    val thumbnailPath: String = "",
    // isSelected is always false in the garden
    val isSelected: Boolean = false,
    val pathVariants: Array<out NodeRef>? = null,
) {

    constructor(type: NodeRootInfo, sorting: NodeSorting, vararg pathVariants: NodeRef)
            : this(type, pathVariants.first().toRoot(type), sorting, pathVariants = pathVariants.takeIf { it.size > 1 })

    val id: NodeId = item.uniqueId + info.temp
    val isEnabled: Boolean get() = item.isCached || info is NodeRootInfo.Storage
    val withPreview: Boolean = previewSorting != null

    init {
        require(item.children?.isOpened != true)
    }

    override fun equals(other: Any?): Boolean = when {
        other !is NodeRoot -> false
        other.info != info -> false
        other.thumbnail != thumbnail -> false
        other.isSelected != isSelected -> false
        !other.item.areContentsTheSame(item) -> false
        else -> true
    }

    override fun hashCode(): Int = hash(info, thumbnail)
}
