package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.extension.mutableCopy
import app.atomofiron.common.util.flow.DataFlow
import app.atomofiron.searchboxapp.utils.EmptyMutableList

private const val UNSELECTED_ROOT_ID = 0
private val EmptyMutableList = EmptyMutableList<NodeRef>()

data class NodeTab(
    val key: NodeTabKey,
    val roots: MutableList<NodeRoot>,
    val states: MutableList<NodeStateImpl>,
    val mimeTypes: List<String> = emptyList(),
) {
    private val _trees = mutableMapOf<NodeId, MutableList<NodeRef>>() // one NodeRef isn't flexible too much
    val trees: Map<NodeId, MutableList<NodeRef>> = _trees
    var selectedRootId = UNSELECTED_ROOT_ID
        private set
    var generation = 0
        private set
    val tree: MutableList<NodeRef> get() = _trees[selectedRootId] ?: EmptyMutableList
    private val _sorting = mutableMapOf<NodeId, NodeSorting>()
    val checked = mutableListOf<Int>()
    val flow = DataFlow(NodeTabItems(emptyList(), emptyList(), null))

    fun NodeRoot.isSelected(): Boolean = id == selectedRootId

    fun getSelectedRoot(): NodeRoot? = roots.find { it.isSelected() }

    fun getSorting(rootId: NodeId): NodeSorting {
        return _sorting.getOrPut(rootId) {
            roots.find { it.id == rootId }
                ?.previewSorting
                ?: NodeSorting.Name
        }
    }

    fun selected(root: NodeRoot): Boolean = root.isSelected()

    fun deselectRoot() {
        selectedRootId = UNSELECTED_ROOT_ID
    }

    fun select(root: NodeRoot) {
        selectedRootId = root.id
    }

    fun incrementGeneration() {
        generation++
    }

    fun setSorting(rootId: NodeId, sorting: NodeSorting) {
        _sorting[rootId] = sorting
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

    fun putTree(id: NodeId, tree: List<NodeRef>) {
        _trees[id] = tree.toMutableList()
    }
}