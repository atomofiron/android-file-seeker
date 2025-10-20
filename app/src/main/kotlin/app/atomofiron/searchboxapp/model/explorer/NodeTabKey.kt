package app.atomofiron.searchboxapp.model.explorer

sealed interface NodeTabKey {
    companion object {
        val Stub: NodeTabKey = Explorer(index = 0, null)
    }

    data class Explorer(
        val index: Int,
        val pickerTypes: List<String>?,
    ) : NodeTabKey

    data class Result(val id: Int) : NodeTabKey
}