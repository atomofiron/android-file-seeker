package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.flow.DataFlow

const val UNSELECTED_ROOT_ID = 0

class NodeTab(
    val key: NodeTabKey,
    val states: MutableList<NodeState>,
) {
    val roots = mutableListOf<NodeRoot>()
    var selectedRootId = UNSELECTED_ROOT_ID
    val tree = mutableListOf<Node>()
    val checked = mutableListOf<Int>()
    val flow = DataFlow<NodeTabItems>()

    fun NodeRoot.isSelected(): Boolean = stableId == selectedRootId

    fun getSelectedRoot(): NodeRoot? = roots.find { it.isSelected() }

    fun selected(root: NodeRoot): Boolean = root.isSelected()
}