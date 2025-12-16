package app.atomofiron.searchboxapp.model.explorer

sealed class NodeTabKey(val primary: Boolean = false) {

    data class Explorer(
        val index: Int,
        val pickerTypes: List<String>? = null,
    ) : NodeTabKey(primary = pickerTypes == null)

    data class Result(val id: Int) : NodeTabKey()
}