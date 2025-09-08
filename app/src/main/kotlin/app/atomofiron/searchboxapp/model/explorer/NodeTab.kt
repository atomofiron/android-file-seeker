package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.searchboxapp.utils.EmptyMutableList
import kotlinx.coroutines.flow.MutableStateFlow

private const val UNSELECTED_ROOT_ID = 0
private val EmptyMutableList = EmptyMutableList<Node>()

class NodeTab(
    val key: NodeTabKey,
    val roots: List<NodeRoot>,
    val states: MutableList<NodeState>,
) {
    val trees = mutableMapOf<Int, MutableList<Node>>()
    var selectedRootId = UNSELECTED_ROOT_ID
        private set
    val tree: MutableList<Node> get() = trees[selectedRootId] ?: EmptyMutableList
    val checked = mutableListOf<Int>()
    val flow = MutableStateFlow(NodeTabItems(emptyList(), emptyList(), null))

    fun NodeRoot.isSelected(): Boolean = stableId == selectedRootId

    fun getSelectedRoot(): NodeRoot? = roots.find { it.isSelected() }

    fun selected(root: NodeRoot): Boolean = root.isSelected()

    fun hasSelectedRoot() = selectedRootId != UNSELECTED_ROOT_ID

    fun deselectRoot() {
        selectedRootId = UNSELECTED_ROOT_ID
    }

    fun select(root: NodeRoot) {
        selectedRootId = root.stableId
    }

    fun Node.opened(): Boolean = roots.find { it.item.uniqueId == rootId }
        ?.let { trees[it.stableId] }
        ?.any { it.uniqueId == uniqueId }
        .let { it == true }
}