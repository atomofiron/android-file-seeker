package app.atomofiron.searchboxapp.model.explorer

sealed interface NodeTabKey {
    companion object {
        val Stub: NodeTabKey = Explorer(primary = true, index = 0)
    }

    data class Explorer(
        val primary: Boolean,
        val index: Int,
    ) : NodeTabKey

    data class Result(val id: Int) : NodeTabKey
}