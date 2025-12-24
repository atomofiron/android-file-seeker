package app.atomofiron.searchboxapp.model.explorer

typealias ExplorerTabKey = NodeTabKey.Explorer

sealed class NodeTabKey(val primary: Boolean = false) {

    data class Explorer(
        val index: Int,
        val pickerTypes: List<String>? = null,
    ) : NodeTabKey(primary = pickerTypes == null)

    data class Result(val id: Int) : NodeTabKey()
}