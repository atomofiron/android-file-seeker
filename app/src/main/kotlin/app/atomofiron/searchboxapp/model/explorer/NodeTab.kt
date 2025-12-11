package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.extension.mutableCopy
import app.atomofiron.common.util.flow.DataFlow
import app.atomofiron.searchboxapp.utils.EmptyMutableList

private const val UNSELECTED_ROOT_ID = 0
private val EmptyMutableList = EmptyMutableList<Node>()

data class NodeTab(
    val key: NodeTabKey,
    val roots: List<NodeRoot>,
    val states: MutableList<NodeStateImpl>,
    val mimeTypes: List<String> = emptyList(),
) {
    private val _trees = mutableMapOf<NodeId, MutableList<Node>>() // todo replace Node with NodeRef and use NodeRoot.item only?
    val trees: Map<NodeId, MutableList<Node>> = _trees
    var selectedRootId = UNSELECTED_ROOT_ID
        private set
    var generation = 0
        private set
    val tree: MutableList<Node> get() = _trees[selectedRootId] ?: EmptyMutableList
    val checked = mutableListOf<Int>()
    val flow = DataFlow(NodeTabItems(emptyList(), emptyList(), null))

    fun NodeRoot.isSelected(): Boolean = id == selectedRootId

    fun getSelectedRoot(): NodeRoot? = roots.find { it.isSelected() }

    fun selected(root: NodeRoot): Boolean = root.isSelected()

    fun hasSelectedRoot() = selectedRootId != UNSELECTED_ROOT_ID

    fun deselectRoot() {
        selectedRootId = UNSELECTED_ROOT_ID
    }

    fun select(root: NodeRoot) {
        selectedRootId = root.id
    }

    fun incrementGeneration() {
        generation++
    }

    fun Node.opened(): Boolean = roots.find { it.item.uniqueId == rootId }
        ?.let { _trees[it.id] }
        ?.any { it.uniqueId == uniqueId }
        .let { it == true }

    fun clone(key: NodeTabKey, mimeTypes: List<String>): NodeTab {
        val copy = copy(key = key, mimeTypes = mimeTypes)
        copy.selectedRootId = selectedRootId
        _trees.forEach { (key, tree) ->
            copy._trees[key] = tree.mutableCopy()
        }
        return copy
    }

    fun putTree(id: NodeId, tree: List<Node>) {
        _trees[id] = tree.toMutableList()
    }
}