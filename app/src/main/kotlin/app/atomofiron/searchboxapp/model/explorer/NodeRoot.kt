package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.extension.hash
import app.atomofiron.searchboxapp.model.explorer.other.Thumbnail
import app.atomofiron.searchboxapp.utils.ExplorerUtils.toRoot

data class NodeRoot(
    val info: NodeRootInfo,
    val item: Node,
    val defaultSorting: NodeSorting,
    val thumbnail: Thumbnail?,
    val sources: Array<out NodeRootSrc>? = null,
    val thumbnailPath: String = "",
    // isSelected is always false in the garden
    val isSelected: Boolean = false,
) {
    constructor(
        info: NodeRootInfo,
        defaultSorting: NodeSorting,
        thumbnail: Thumbnail? = null,
        vararg sources: NodeRootSrc,
    ) : this(info, NodeRef.Stub.toRoot(info, uniqueId = info.id), defaultSorting, thumbnail, sources)

    constructor(
        info: NodeRootInfo,
        defaultSorting: NodeSorting,
        ref: NodeRef,
        thumbnail: Thumbnail? = null,
    ) : this(info, ref.toRoot(info), defaultSorting, thumbnail)

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
