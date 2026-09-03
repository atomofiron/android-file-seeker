package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.extension.debugRequire

private val StateStub = NodeStateImpl()

data class Node(
    val ref: NodeRef,
    val parentRef: NodeRef = ref.parent,
    val uniqueId: NodeId = ref.uniqueId,
    val rootId: Int = uniqueId,
    val children: NodeChildren? = null,

    val meta: NodeMeta = NodeMeta.Empty,
    val content: NodeContent,
    val error: NodeError? = null,
    // state is always stateStub in the garden
    val state: NodeStateImpl = StateStub,
    // isChecked is always false in the garden
    val isChecked: Boolean = false,
    // isDeepest is always false in the garden
    val isDeepest: Boolean = false,
    // generation is always 0 in the garden
    val generation: Int = 0,
) : NodeMetaData by meta, NodeState by state {

    val name get() = ref.name
    val lowercaseName get() = ref.lowercaseName
    val path get() = ref.string
    val isRoot: Boolean = uniqueId == rootId

    val isDirectory: Boolean = content is NodeContent.Directory
    val isFile: Boolean = content is NodeContent.File

    val isCached: Boolean get() = hasChildren || !isDirectory && content.isCached
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
        other.meta != meta -> false
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
        other.children?.filteredOut != children?.filteredOut -> false
        isOpened && other.getOpenedId() != getOpenedId() -> false
        other.content != content -> false
        else -> true
    }

    override fun hashCode(): Int = uniqueId

    override fun equals(other: Any?): Boolean = when {
        other !is Node -> false
        !areContentsTheSame(other) -> false
        else -> true
    }

    fun getOpenedId(): NodeId = children?.find { it.isOpened }?.uniqueId ?: 0

    fun closed() = when {
        children == null -> this
        isOpened -> copy(children = children.copy(isOpened = false), isDeepest = false)
        else -> this
    }

    fun mutate(
        ref: NodeRef,
        parentRef: NodeRef = ref.parent,
        meta: NodeMeta = this.meta,
        error: NodeError? = this.error,
    ): Node {
        val new = copy(ref = ref, parentRef = parentRef, uniqueId = ref.uniqueId, meta = meta, state = state, error = error)
        val children = new.children?.items
        children?.forEachIndexed { i, item ->
            if (item.parentRef != ref) {
                children[i] = item.mutate(ref = ref + item.name)
            }
        }
        return new
    }
}
