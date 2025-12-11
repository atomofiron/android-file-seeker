package app.atomofiron.searchboxapp.model.explorer

sealed class NodeTabKey(val primary: Boolean = false) {

    data class Explorer(
        val index: Int,
        val pickerTypes: List<String>?,
    ) : NodeTabKey(primary = pickerTypes == null) {
        companion object {
            val Stub = Explorer(index = 0, null)
        }
    }

    data class Result(val id: Int) : NodeTabKey()
}