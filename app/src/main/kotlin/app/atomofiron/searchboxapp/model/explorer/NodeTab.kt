package app.atomofiron.searchboxapp.model.explorer

import kotlinx.coroutines.flow.MutableStateFlow

private const val UNSELECTED_ROOT_ID = 0

class NodeTab(
    val key: NodeTabKey,
    val roots: List<NodeRoot>,
    val states: MutableList<NodeState>,
) {
    var selectedRootId = UNSELECTED_ROOT_ID
        private set
    val tree = mutableListOf<Node>()
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
}