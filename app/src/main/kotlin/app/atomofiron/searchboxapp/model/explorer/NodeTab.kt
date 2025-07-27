package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.flow.DataFlow

class NodeTab(
    val key: NodeTabKey,
    val states: MutableList<NodeState>,
) {
    val roots = mutableListOf<NodeRoot>()
    var selectedRootId = 0
    val tree = mutableListOf<Node>()
    val checked = mutableListOf<Int>()
    val flow = DataFlow<NodeTabItems>()

    fun NodeRoot.isSelected(): Boolean = stableId == selectedRootId

    fun getSelectedRoot(): NodeRoot? = roots.find { it.isSelected() }

    fun selected(root: NodeRoot): Boolean = root.isSelected()
}