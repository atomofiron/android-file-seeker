package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.extension.mutableCopy
import app.atomofiron.common.util.flow.DataFlow
import app.atomofiron.searchboxapp.utils.EmptyMutableList

private const val UNSELECTED_ROOT_ID = 0
private val EmptyMutableList = EmptyMutableList<NodeRef>()

data class NodeTab private constructor(
    val key: NodeTabKey,
    val roots: MutableList<NodeRoot>,
    val states: MutableMap<NodeId, NodeStateImpl>,
    val mimeTypes: List<String>,
) {
    val trees: Map<NodeId, MutableList<NodeRef>>
        field = mutableMapOf() // one NodeRef isn't flexible too much
    var selectedRootId = UNSELECTED_ROOT_ID
        private set
    var generation = 0
        private set
    val tree: MutableList<NodeRef> get() = trees[selectedRootId] ?: EmptyMutableList
    private val sorting = mutableMapOf<NodeId, NodeSorting>()
    val checked = mutableListOf<NodeId>()
    val flow = DataFlow(NodeTabItems(emptyList(), emptyList(), null))

    constructor(
        key: NodeTabKey,
        roots: MutableList<NodeRoot>,
        states: MutableMap<NodeId, NodeStateImpl>,
    ) : this(key, roots, states, emptyList())

    fun NodeRoot.isSelected(): Boolean = id == selectedRootId

    fun getSelectedRoot(): NodeRoot? = roots.find { it.isSelected() }

    fun getSortingForSelected(): NodeSorting = getSorting(selectedRootId)

    fun getSorting(rootId: NodeId): NodeSorting {
        return sorting.getOrPut(rootId) {
            roots.find { it.id == rootId }
                ?.defaultSorting
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
        this.sorting[rootId] = sorting
    }

    fun Node.opened(): Boolean = roots.find { it.item.uniqueId == rootId }
        ?.let { trees[it.id] }
        ?.any { it.uniqueId == uniqueId }
        .let { it == true }

    fun clone(key: NodeTabKey, mimeTypes: List<String>): NodeTab {
        val copy = copy(key = key, mimeTypes = mimeTypes)
        copy.selectedRootId = selectedRootId
        copy.sorting.putAll(sorting)
        trees.forEach { (key, tree) ->
            copy.putTree(key, tree.mutableCopy())
        }
        return copy
    }

    fun putTree(id: NodeId, tree: List<NodeRef>) {
        trees[id] = tree.toMutableList()
    }
}