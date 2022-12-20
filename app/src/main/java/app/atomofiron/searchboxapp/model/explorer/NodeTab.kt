package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.flow.DataFlow
import java.util.*

class NodeTabTree(
    val key: NodeTabKey,
    val states: MutableList<NodeState>,
) {

    val roots = mutableListOf<NodeRoot>()
    val tree = mutableListOf<NodeLevel>()
    val checked: MutableList<Int> = LinkedList()
    val flow = DataFlow<NodeTabItems>()
}