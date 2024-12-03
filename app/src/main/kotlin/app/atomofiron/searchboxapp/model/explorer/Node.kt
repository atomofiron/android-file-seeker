package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.searchboxapp.utils.ExplorerDelegate.areChildrenContentsTheSame
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.name
import app.atomofiron.searchboxapp.utils.ExplorerDelegate.parent


data class Node constructor(
    val path: String,
    val parentPath: String = path.parent(),
    val uniqueId: Int = path.toUniqueId(),
    val rootId: Int = uniqueId,
    val children: NodeChildren? = null,

    val properties: NodeProperties = NodeProperties(name = path.name()),
    val content: NodeContent,
    val error: NodeError? = null,
    // в дереве всегда state = stateStub
    val state: NodeState = stateStub,
    // в дереве всегда isChecked = false
    val isChecked: Boolean = false,
    // в дереве всегда isCurrent = false
    val isCurrent: Boolean = false,
) : INodeProperties by properties, INodeState by state {
    companion object {
        private val stateStub = NodeState(0)
        fun String.toUniqueId(): Int = hashCode()
    }
    val isRoot: Boolean = uniqueId == rootId

    val isDirectory: Boolean = content is NodeContent.Directory
    val isFile: Boolean = content is NodeContent.File

    val isCached: Boolean get() = children != null
    val isEmpty: Boolean get() = children?.isEmpty() == true
    val isOpened: Boolean get() = children?.isOpened == true
    val childCount: Int get() = children?.size ?: 0

    fun areContentsTheSame(other: Node?): Boolean = when {
        other == null -> false
        other.uniqueId != uniqueId -> false
        other.path != path -> false
        other.rootId != rootId -> false
        other.properties != properties -> false
        other.state != state -> false
        other.error != error -> false
        other.isCached != isCached -> false
        other.isEmpty != isEmpty -> false
        other.isOpened != isOpened -> false
        other.isDirectory != isDirectory -> false
        other.isFile != isFile -> false
        other.isChecked != isChecked -> false
        other.isCurrent != isCurrent -> false
        other.content != content -> false
        else -> true
    }

    override fun hashCode(): Int = uniqueId

    override fun equals(other: Any?): Boolean = when {
        other !is Node -> false
        !areContentsTheSame(other) -> false
        else -> other.children.areChildrenContentsTheSame(children)
    }

    fun rename(name: String): Node {
        val path = parentPath + name
        val properties = properties.copy(name = name)
        return copy(path = path, uniqueId = path.toUniqueId(), properties = properties)
    }
}

