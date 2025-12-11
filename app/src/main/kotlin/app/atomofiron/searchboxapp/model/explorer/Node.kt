package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.extension.debugRequire

data class Node(
    val ref: NodeRef,
    val parentRef: NodeRef = ref.parent,
    val uniqueId: NodeId = ref.uniqueId,
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
) : INodeProperties by properties, NodeState by state {
    companion object {
        val stateStub = NodeStateImpl(0)
    }
    val name get() = ref.name
    val isRoot: Boolean = uniqueId == rootId

    val isDirectory: Boolean = content is NodeContent.Directory
    val isFile: Boolean = content is NodeContent.File

    val isCached: Boolean get() = children != null || !isDirectory && content.isCached
    val isEmpty: Boolean? get() = children?.run { size - (filteredOut ?: 0) == 0 }
    val isOpened: Boolean get() = children?.isOpened == true
    val hasChildren: Boolean get() = children != null
    val childCount: Int get() = children?.size ?: 0

    init {
        debugRequire(uniqueId == ref.uniqueId || uniqueId == -ref.uniqueId) { ref.toString() }
    }

    fun areContentsTheSame(other: Node?): Boolean = when {
        other == null -> false
        other === this -> true
        other.uniqueId != uniqueId -> false
        other.ref != ref -> false
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
        else -> true
    }

    fun getOpenedIndex(): Int = children?.indexOfFirst { it.isOpened } ?: -1

    fun closed() = when {
        children == null -> this
        isOpened -> copy(children = children.copy(isOpened = false), isDeepest = false)
        else -> this
    }

    fun mutate(
        ref: NodeRef,
        parentRef: NodeRef = ref.parent,
        properties: NodeProperties = this.properties,
        state: NodeState = this.state,
        error: NodeError? = this.error,
    ): Node {
        val new = copy(ref = ref, parentRef = parentRef, uniqueId = ref.uniqueId, properties = properties, state = state, error = error)
        val children = new.children?.items
        children?.forEachIndexed { i, it ->
            val item = children[i]
            if (item.parentRef != ref) {
                children[i] = item.mutate(ref = ref + item.name)
            }
        }
        return new
    }
}
