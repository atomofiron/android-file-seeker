package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.extension.hash
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toRoot

data class NodeRoot(
    val info: NodeRootInfo,
    val item: Node,
    val defaultSorting: NodeSorting,
    val thumbnail: Thumbnail?,
    val thumbnailPath: String = "",
    // isSelected is always false in the garden
    val isSelected: Boolean = false,
    val pathVariants: Array<out NodeRef>? = null,
) {
    constructor(
        type: NodeRootInfo,
        defaultSorting: NodeSorting,
        thumbnail: Thumbnail? = null,
        vararg pathVariants: NodeRef,
    ) : this(type, pathVariants.first().toRoot(type), defaultSorting, thumbnail, pathVariants = pathVariants.takeIf { it.size > 1 })

    val id: NodeId = item.uniqueId
    val isEnabled: Boolean get() = item.isCached || info is NodeRootInfo.Storage
    val withPreview: Boolean = thumbnail != null

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
