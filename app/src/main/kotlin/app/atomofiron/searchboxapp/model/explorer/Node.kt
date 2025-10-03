package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.extension.debugRequire
import app.atomofiron.searchboxapp.utils.ExplorerUtils.areChildrenContentsTheSame

data class Node(
    val path: NodePath,
    val parentPath: NodePath = path.parent,
    val uniqueId: Int = path.uniqueId,
    val rootId: Int = uniqueId,
    val children: NodeChildren? = null,

    val properties: NodeProperties = NodeProperties(),
    val content: NodeContent,
    val error: NodeError? = null,
    // state is always stateStub in the garden
    val state: NodeState = stateStub,
    // isChecked is always false in the garden
    val isChecked: Boolean = false,
    // isDeepest is always false in the garden
    val isDeepest: Boolean = false,
    // generation is always 0 in the garden
    val generation: Int = 0,
) : INodeProperties by properties, INodeState by state {
    companion object {
        val stateStub = NodeState(0)
    }
    val name get() = path.name
    val isRoot: Boolean = uniqueId == rootId

    val isDirectory: Boolean = content is NodeContent.Directory
    val isFile: Boolean = content is NodeContent.File

    val isCached: Boolean get() = children != null || !isDirectory && content.isCached
    val isEmpty: Boolean? get() = children?.isEmpty()
    val isOpened: Boolean get() = children?.isOpened == true
    val hasChildren: Boolean get() = children != null
    val childCount: Int get() = children?.size ?: 0

    init {
        debugRequire(uniqueId == path.uniqueId || uniqueId == -path.uniqueId) { path.toString() }
    }

    fun areContentsTheSame(other: Node?): Boolean = when {
        other == null -> false
        other === this -> true
        other.uniqueId != uniqueId -> false
        other.path != path -> false
        other.rootId != rootId -> false
        other.properties != properties -> false
        other.state.operation != state.operation -> false
        other.error != error -> false
        other.isCached != isCached -> false
        other.isEmpty != isEmpty -> false
        other.isOpened != isOpened -> false
        other.isDirectory != isDirectory -> false
        other.isFile != isFile -> false
        other.isChecked != isChecked -> false
        other.isDeepest != isDeepest -> false
        other.hasChildren != hasChildren -> false
        other.childCount != childCount -> false
        isOpened && other.getOpenedIndex() != getOpenedIndex() -> false
        other.content != content -> false
        else -> true
    }

    override fun hashCode(): Int = uniqueId

    override fun equals(other: Any?): Boolean = when {
        other !is Node -> false
        !areContentsTheSame(other) -> false
        else -> other.children.areChildrenContentsTheSame(children)
    }

    fun getOpenedIndex(): Int = children?.indexOfFirst { it.isOpened } ?: -1

    fun closed() = when {
        children == null -> this
        isOpened -> copy(children = children.copy(isOpened = false), isDeepest = false)
        else -> this
    }
}
