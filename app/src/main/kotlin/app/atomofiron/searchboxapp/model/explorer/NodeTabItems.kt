package app.atomofiron.searchboxapp.model.explorer

data class NodeTabItems(
    val roots: List<NodeRoot>,
    val items: List<Node>,
    val deepest: Node?,
)