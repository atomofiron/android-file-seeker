package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.flow.DataFlow
import java.util.*

class NodeTab(
    val key: NodeTabKey,
    val states: MutableList<NodeState>,
) {
    val roots = mutableListOf<NodeRoot>()
    var selectedRootId = 0
    val tree = mutableListOf<Node>()
    val checked: MutableList<Int> = LinkedList()
    val flow = DataFlow<NodeTabItems>()
}